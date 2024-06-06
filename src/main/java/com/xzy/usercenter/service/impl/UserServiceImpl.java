package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xzy.usercenter.common.ErrorCode;
import com.xzy.usercenter.exception.BusinessException;
import com.xzy.usercenter.model.domain.User;
import com.xzy.usercenter.service.UserService;
import com.xzy.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.xzy.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.xzy.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现类
 *
 * @author xzy
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值：混淆密码
     */
    private static final String SALT = "yupi";

    /**
     * 用户登录态值
     */
    /*
    private static final String USER_LOGIN_STATE = "userLoginState";*/

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @param planetCode    星球编号
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验用户的账户、密码、校验密码，是否符合要求
        // 1.1非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 1.2账户长度不小4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 1.3密码和校验密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }


        // 1.5账户不包含特殊字符(正则表达式)
        String regex = "^[a-zA-Z0-9]*$";
        boolean res = userAccount.matches(regex);
        if (!res) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名不符合规则");
        }

        // 1.6密码和校验码相同
        boolean equals = StringUtils.equals(userPassword, checkPassword);
        if (!equals) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码和校验码不相同");
        }

        //星球编号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号重复");
        }


        // 1.4账户不能重复(查询数据库，放在最后一位校验，避免操作数据库)
        queryWrapper = new QueryWrapper<>();
        QueryWrapper<User> wrapper = queryWrapper.eq("userAccount", userAccount);
        count = userMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户重复");
        }

        // 2.对密码进行加密(md5加密，密码千万不要直接以明文存储到数据库中)
        String verifyPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        // 3.向数据库中插入用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(verifyPassword);
        user.setPlanetCode(planetCode);

        int num = userMapper.insert(user);
        if (num < 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "新增用户失败");
        }

        //返回用户id
        return user.getId();
    }

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验用户账户和密码是否合法
        // 1.1非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }

        // 1.2账户长度不小于4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }

        // 1.3密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }

        // 1.4账户不包含特殊字符
        String regex = "^[a-zA-Z0-9]*$";
        boolean res = userAccount.matches(regex);
        if (!res) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账户名不符合规则");
        }

        // 2.校验密码是否输入正确，要和数据库中的密文密码对比
        String verifyPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", verifyPassword);
        // 这里同样也会把逻辑删除的用户查询出来(mybatisplus逻辑删除)
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("用户账户和密码不匹配");
            return null;
        }

        // 3.用户信息脱敏，防止数据库中的字段泄露
        User safetyuser = getSafetyUser(user);

        // 4.记录用户登录态（Sesssion），将其保存到服务器上
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyuser);

        // 5.返回脱敏后的信息
        return safetyuser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser 原始用户
     * @return 安全用户
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safetyuser = new User();
        safetyuser.setId(originUser.getId());
        safetyuser.setUsername(originUser.getUsername());
        safetyuser.setUserAccount(originUser.getUserAccount());
        safetyuser.setAvatarUrl(originUser.getAvatarUrl());
        safetyuser.setGender(originUser.getGender());
        //safetyuser.setUserPassword(""); 敏感信息不返回
        safetyuser.setPlanetCode(originUser.getPlanetCode());
        safetyuser.setPhone(originUser.getPhone());
        safetyuser.setEmail(originUser.getEmail());
        safetyuser.setUserRole(originUser.getUserRole());
        safetyuser.setUserStatus(originUser.getUserStatus());
        safetyuser.setCreateTime(originUser.getCreateTime());
        safetyuser.setTags(originUser.getTags());
        return safetyuser;
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public Integer userLogout(HttpServletRequest request) {
        //移除登陆态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 获取当前用户
     *
     * @param request
     * @return
     */
    @Override
    public User getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;

        if (user == null) {
            return null;
        }

        //数据库中查询用户，并脱敏返回
        Long id = user.getId();
        User originUser = userMapper.selectById(id);
        return getSafetyUser(originUser);
    }

    /**
     * 根据标签列表搜索用户
     *
     * @param tagNameList 用户拥有的标签
     * @return
     */
    @Override
    public List<User> searchUserByTags(List<String> tagNameList) {
        // 判断参数的合法性
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 先进行一次空查询，避免因数据库连接耗时影响比较结果。对比多次，调换查询的顺序
        /*QueryWrapper queryWrapper = new QueryWrapper();
        userMapper.selectCount(null);*/

        // 一：内存查询
        //long start = System.currentTimeMillis();
        List<User> userList2 = searchByMemory(tagNameList);   //这里我们使用内存查询（更灵活）
        //System.out.println("内存 查询花费的时间为：" + (System.currentTimeMillis() - start) + "ms");


        //二：SQL查询
        //start = System.currentTimeMillis();
        //List<User> userList1 = searchBySql(tagNameList);    //这里我们使用内存查询（更灵活）
        //System.out.println("SQL 查询花费的时间为：" + (System.currentTimeMillis() - start) + "ms");

        // 判断两集合是否相同
        /*if (userList1.retainAll(userList2)){
            log.info("两集合相同");
        }else {
            log.info("两集合不相同");
        }*/

        return userList2;
    }

    /**
     * 修改用户信息
     *
     * @param user      要修改的信息
     * @param loginUser 登录的用户
     * @return
     */
    @Override
    public Integer updateUser(User user, User loginUser) {
        long userId = user.getId();

        if (userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 如果用户没有传入任何要更新的值，就直接返回报错，不用执行uodate语句
        // 1.如果是管理员，允许更新任意用户
        // 2.不是管理员，则只能更新自己的信息
        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 判断是否是管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 判断是管理员才可以查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否是管理员
     *
     * @param LoginUser
     * @return
     */
    @Override
    public boolean isAdmin(User LoginUser) {
        return LoginUser != null && LoginUser.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 获取登录用户的信息
     *
     * @param request
     * @return
     */
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return (User) userObj;
    }

    /**
     * 根据内存查询
     *
     * @param tagNameList
     * @return
     */
    private List<User> searchByMemory(List<String> tagNameList) {
        // 1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        // 2.在内存中判断是否包含要查询的标签
        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            // 2.1 tagsStr不能为空
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            // 2.2 序列化
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());

            // 2.3 判断标签列表是否为空，为空则将默认值付给tempTagNameSet
            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            for (String tagName : tagNameList) {
                // 2.4 判断标签列表中是否存在tagName
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 根据sql查询
     *
     * @param tagNameList
     * @return
     */
    private List<User> searchBySql(List<String> tagNameList) {
        // 1. sql 查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 1.1 拼接 and 查询
        // like "%Java%" and like "%Python%"
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        // 2. 返回脱敏的用户集
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }
}