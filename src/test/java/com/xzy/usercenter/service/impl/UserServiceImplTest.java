package com.xzy.usercenter.service.impl;

import com.xzy.usercenter.service.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@SpringBootTest
class UserServiceImplTest {

    @Resource
    private UserService userService;

    @Test
    public void testReges() {
        // 测试用户账户不包含特殊字符（只有字母，数字）
        String regex = "^[a-zA-Z0-9]*$";
        String name = "xz_y";

        boolean b = name.matches(regex);
        System.out.println(b);
    }

    @Test
    public void testjiama() {
        String userPassword = "123";
        String SALT = "xzy";
        String md5DigestAsHex = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes(StandardCharsets.UTF_8));
        System.out.println(md5DigestAsHex);
    }

    @Test
    void testuserRegister() {
        String userAccount = "";
        String userPassword = "12345678";
        String checkPassword = "12345678";

        long res = userService.userRegister(userAccount, userPassword, checkPassword);

        // 用户账户为空 || 用户密码为空 || 校验码为空
        Assertions.assertEquals(-1, res);

        // 账户长度不小于4位
        userAccount = "xzy";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, res);

        // 密码和校验密码不小于8位
        userPassword = "123";
        checkPassword = "123";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, res);

        // 账户不包含特殊字符
        userAccount = "lixiang_one";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, res);

        // 密码和校验码相同
        userPassword = "12345678";
        checkPassword = "123456780";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, res);

        // 账户不能重复
        userAccount = "lixiang";
        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1, res);

        // 成功插入一条数据
        userAccount = "zhangsan";
        userPassword = "12345678";
        checkPassword = "12345678";

        res = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertTrue(res > 0);
    }
}