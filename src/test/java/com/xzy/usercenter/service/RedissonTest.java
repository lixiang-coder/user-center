package com.xzy.usercenter.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * RedissonClient提供了大量的分布式数据集来简化对 Redis 的操作和使用，
 * 可以让开发者像使用本地集合一样使用 Redis，完全感知不到 Redis 的存在。
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    public void test() {
        //list集合：数据存储在本地JVM内存中
        List<String> list= new ArrayList<>();
        list.add("xzy");
        System.out.println("list:" + list);

        //RedissonClient数据存储再redis内存中
        RList<String> rList = redissonClient.getList("test_list");
        rList.add("lyw");
        System.out.println("rList:" + rList);
    }
}
