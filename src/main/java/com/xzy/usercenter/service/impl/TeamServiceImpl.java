package com.xzy.usercenter.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xzy.usercenter.mapper.TeamMapper;
import com.xzy.usercenter.model.domain.Team;
import com.xzy.usercenter.service.TeamService;
import org.springframework.stereotype.Service;

/**
 * @author 86132
 * @description 针对表【team(队伍)】的数据库操作Service实现
 * @createDate 2024-06-06 09:03:04
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

}




