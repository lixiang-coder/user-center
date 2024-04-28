package com.xzy.usercenter.service;

import com.xzy.usercenter.model.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest

class UserServiceTest {
    @Resource
    private UserService userService;

    @Test
    public void testsave() {
        User user = new User();
        user.setId(0L);
        user.setUsername("xzy");
        user.setUserAccount("xzy");
        user.setAvatarUrl("https://th.bing.com/th/id/R.fd81516a06ce33c15b194494272fa6e9?rik=XAfnJ6A9NFvAyA&riu=http%3a%2f%2fimg.touxiangwu.com%2fuploads%2fallimg%2f2022053117%2fivhiashhpu1.jpg&ehk=Yi2aDhWvd0rnBKl1xloJy8F1RfGd8%2bcC75k4ff8dVXk%3d&risl=&pid=ImgRaw&r=0");
        user.setGender(0);
        user.setUserPassword("1223");
        user.setPhone("13279487597");
        user.setEmail("2894702202@qq.com");
        user.setUserStatus(0);

        boolean res = userService.save(user);
        System.out.println(res);


    }
}