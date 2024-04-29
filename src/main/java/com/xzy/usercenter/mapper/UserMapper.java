package com.xzy.usercenter.mapper;

import com.xzy.usercenter.model.domain.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 86132
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2024-04-28 21:50:08
* @Entity com.xzy.usercenter.model.domain.User
*/

@Mapper
public interface UserMapper extends BaseMapper<User> {

}




