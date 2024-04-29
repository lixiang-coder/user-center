package com.xzy.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xzy.usercenter.model.domain.User;
import com.xzy.usercenter.model.domain.request.UserLoginRequest;
import com.xzy.usercenter.model.domain.request.UserRegisterRequest;
import com.xzy.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.xzy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.xzy.usercenter.contant.UserConstant.USER_LOGIN_STATE;


/**
 * 用户接口
 *
 * @author xzy
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class userController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 请求体
     * @return 新用户id
     */
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            // todo 后续优化一下返回值
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            // todo 后续优化一下返回值
            return null;
        }

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }


    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            // todo 后续优化一下返回值
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            // todo 后续优化一下返回值
            return null;
        }

        return userService.userLogin(userAccount, userPassword, request);
    }


    /**
     * 用户注销
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        if (request == null) {
            // todo 后续优化一下返回值
            return null;
        }
        return userService.userLogout(request);
    }

    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        return userService.getCurrentUser(request);
    }


    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 查询到的用户集
     */
    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request) {
        // 判断是管理员才可以查询
        if (!isAdmin(request)) {
            log.info("管理员才可查询用户");
            return new ArrayList<>();
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
        return safetyUserList;
    }


    /**
     * 删除用户
     *
     * @param id      要删除用户的id
     * @param request session
     * @return 是否删除成功
     */
    @PostMapping("/delete")
    public boolean deleteUser(@RequestBody long id, HttpServletRequest request) {
        //判断是管理员才可以删除
        if (!isAdmin(request)) {
            log.info("管理员才可删除用户");
            return false;
        }
        if (id <= 0) {
            return false;
        }

        return userService.removeById(id);
    }

    /**
     * 判断是否是管理员
     *
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        // 判断是管理员才可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

}
