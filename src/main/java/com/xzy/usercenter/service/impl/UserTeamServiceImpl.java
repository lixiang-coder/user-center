package com.xzy.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.mapper.UserTeamMapper;
import com.xzy.usercenter.model.domain.UserTeam;
import com.xzy.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 86132
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-06-06 09:04:00
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




