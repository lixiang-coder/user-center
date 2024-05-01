package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

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
     * @param planetCode 星球编号
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
        queryWrapper.eq("planetCode",planetCode);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 1){
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
}




