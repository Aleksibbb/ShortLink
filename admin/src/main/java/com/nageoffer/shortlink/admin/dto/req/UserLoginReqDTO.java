package com.nageoffer.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户登陆请求参数：用户名 + 密码
 */
@Data
public class UserLoginReqDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;
}
