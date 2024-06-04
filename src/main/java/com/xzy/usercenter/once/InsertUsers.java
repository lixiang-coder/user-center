package com.xzy.usercenter.once;

import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;

    /**
     * 批量插入用户
     */
    //@Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("程序开始执行~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        final int INSERT_NUM = 100;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假薛朝阳");
            user.setUserAccount("fakexzy");
            user.setAvatarUrl("https://xzynet.com.cn/wp-content/uploads/2024/03/avatar_01.jpg");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("13279487597");
            user.setEmail("2894702202@qq.com");
            user.setUserRole(0);
            user.setPlanetCode("159");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
