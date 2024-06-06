package com.xzy.usercenter.service;

import com.xzy.usercenter.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xzy.usercenter.model.domain.User;

/**
* @author xzy
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-06-06 09:03:04
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);
}
