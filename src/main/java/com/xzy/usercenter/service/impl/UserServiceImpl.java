package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.model.User;
import com.xzy.usercenter.service.UserService;
import com.xzy.usercenter.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * @author xzy
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-04-28 21:50:08
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

}




