package com.xzy.usercenter.service;

import com.xzy.usercenter.model.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务类
 *
 * @author xzy
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);
}
