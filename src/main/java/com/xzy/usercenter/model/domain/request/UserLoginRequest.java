package com.xzy.usercenter.model.domain.request;

import lombok.Data;

/**
 * 用户登录请求体
 */
@Data
public class UserLoginRequest {
    private String userAccount;
    private String userPassword;
}
