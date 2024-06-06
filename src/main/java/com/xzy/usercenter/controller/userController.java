package com.xzy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xzy.usercenter.common.BaseResponse;
import com.xzy.usercenter.common.ErrorCode;
import com.xzy.usercenter.common.ResultUtils;
import com.xzy.usercenter.exception.BusinessException;
import com.xzy.usercenter.model.domain.User;
import com.xzy.usercenter.model.domain.request.UserLoginRequest;
import com.xzy.usercenter.model.domain.request.UserRegisterRequest;
import com.xzy.usercenter.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.convert.RedisTypeMapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.xzy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.xzy.usercenter.contant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author xzy
 */
@Slf4j
@RestController
@CrossOrigin(origins = {"http://localhost:3000"})
@RequestMapping("/user")
public class userController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 请求体
     * @return 新用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            return null;
        }

        long res = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        //return new BaseResponse<>(0,res,"ok");
        return ResultUtils.success(res);
    }


    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求体
     * @param request          请求参数
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        //return new BaseResponse<>(0,user,"ok");
        return ResultUtils.success(user);
    }


    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Integer res = userService.userLogout(request);
        return ResultUtils.success(res);
    }

    /**
     * 查询当前用户
     *
     * @param request
     * @return
     */
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;

        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();

        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 查询到的用户集
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        // 判断是管理员才可以查询
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        // 返回脱敏后的用户信息
        List<User> userList = userService.list(queryWrapper);
        List<User> safetyUserList = new ArrayList<>();
        for (User user : userList) {
            User safetyUser = userService.getSafetyUser(user);
            safetyUserList.add(safetyUser);
        }
        return ResultUtils.success(safetyUserList);
    }


    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 1.获取登录用户的信息
        User loginUser = userService.getLoginUser(request);
        // 2.获取每个用户id作为redis的key
        String redisKey = String.format("yupao:user:recommend:%s", loginUser.getId());
        // 3.创建redis
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        // 4.先查缓存，如果有，直接读缓存并返回
        Page<User> userPage = (Page<User>) valueOperations.get(redisKey);

        // 4.1缓存中存在，直接返回
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }

        // 4.2缓存中不存在，查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 分页查询用户
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        // 4.3将从数据库中查出的数据存入缓存中，同时设置过期时间（30s）
        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
        // 5.返回数据
        return ResultUtils.success(userPage);
    }

    /**
     * 删除用户
     *
     * @param id      要删除用户的id
     * @param request session
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        //判断是管理员才可以删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
        }
        if (id <= 0) {
            return null;
        }

        //将isDelete从0置为1，而不是真的删除
        boolean res = userService.removeById(id);
        return ResultUtils.success(res);

    }


    /**
     * 根据标签列表名查询用户
     *
     * @param tagNameList 标签列表名
     * @return 用户集
     */
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        // 判断标签列表是否为空
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUserByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    /**
     * 修改用户信息
     *
     * @param user
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Integer result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }
}
