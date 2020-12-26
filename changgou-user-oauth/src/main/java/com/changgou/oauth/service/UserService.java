package com.changgou.oauth.service;

import com.changgou.oauth.pojo.AuthToken;

public interface UserService {

    /**
     * 用户登录
     * @param username 用户
     * @param password
     * @param clientId 客户端
     * @param clientSecret
     * @return
     */
    public AuthToken login(String username,
                           String password,
                           String clientId,
                           String clientSecret);
}
