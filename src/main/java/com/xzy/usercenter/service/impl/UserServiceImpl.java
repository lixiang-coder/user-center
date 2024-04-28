package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import com.xzy.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * 用户服务实现类
 *
 * @author xzy
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    private final String SALT = "daxiaDaxia";

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验用户的账户、密码、校验密码，是否符合要求
        // 1.1非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            // todo 后续优化一下返回值
            return -1L;
        }

        // 1.2账户长度不小4位
        if (userAccount.length() < 4) {
            // todo 后续优化一下返回值
            return -1L;
        }

        // 1.3密码和校验密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            // todo 后续优化一下返回值
            return -1L;
        }


        // 1.5账户不包含特殊字符(正则表达式)
        String regex = "^[a-zA-Z0-9]*$";
        boolean res = userAccount.matches(regex);
        if (!res) {
            // todo 后续优化一下返回值
            return -1L;
        }

        // 1.6密码和校验码相同
        boolean equals = StringUtils.equals(userPassword, checkPassword);
        if (!equals) {
            // todo 后续优化一下返回值
            return -1L;
        }

        // 1.4账户不能重复(查询数据库，放在最后一位校验，避免操作数据库)
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        QueryWrapper<User> wrapper = queryWrapper.eq("userAccount", userAccount);
        Long count = userMapper.selectCount(wrapper);
        if (count > 0) {
            // todo 后续优化一下返回值
            return -1L;
        }

        // 2.对密码进行加密(md5加密，密码千万不要直接以明文存储到数据库中)
        String verifyPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));

        // 3.向数据库中插入用户数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(verifyPassword);

        int num = userMapper.insert(user);
        if (num < 1){
            // todo 后续优化一下返回值
            return -1L;
        }

        //返回用户id
        return user.getId();
    }
}




