package com.xzy.usercenter.service;

import com.xzy.usercenter.mapper.UserMapper;
import com.xzy.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.ComparisonFailure;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;

    /**
     * 批量插入用户
     *
     * 插入10万条数据花费的时间为397368ms 约400s
     */
    @Test
    public void doInsertUsers1() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("程序开始执行~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        final int INSERT_NUM = 100000;
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
        System.out.println("花费的时间为" + stopWatch.getTotalTimeMillis() + "ms");
    }

    /**
     * 批量插入用户
     *
     * 插入10万条数据花费的时间为16489ms 约16s  前提batchSize大小为10000
     * 插入10万条数据花费的时间为17078ms 约17s  前提batchSize大小为50000
     * 插入10万条数据花费的时间为16474ms 约16s  前提batchSize大小为25000
     */
    @Test
    public void doInsertUsers2() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("程序开始执行~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        final int INSERT_NUM = 100000;
        List<User> userList = new ArrayList<>();
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
            userList.add(user);
        }
        userService.saveBatch(userList, 25000);
        stopWatch.stop();
        System.out.println("花费的时间为" + stopWatch.getTotalTimeMillis() + "ms");
    }


    /**
     * 并发批量插入用户
     *
     * 插入10万条数据花费的时间为4917ms 约5s
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        System.out.println("程序开始执行~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        final int INSERT_NUM = 100000;
        // 分十组
        int j = 0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
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
                userList.add(user);
                if (j % 10000 == 0){
                    break;
                }
            }
            // 异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println("threadName" + Thread.currentThread().getName());
                userService.saveBatch(userList, 25000);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println("花费的时间为" + stopWatch.getTotalTimeMillis() + "ms");
    }

}
