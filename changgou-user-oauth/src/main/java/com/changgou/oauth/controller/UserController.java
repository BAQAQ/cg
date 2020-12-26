package com.changgou.oauth.controller;

import com.changgou.oauth.pojo.AuthToken;
import com.changgou.oauth.service.UserService;
import com.changgou.oauth.util.CookieTools;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Value("${auth.clientId}")
    private  String clientId;
    @Value("${auth.clientSecret}")
    private  String clientSecret;
    @Value("${auth.cookieDomain}")
    private  String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    private  Integer cookieMaxAge;
    @Value("${auth.ttl}")
    private  String ttl;
    @Autowired
    private HttpServletResponse response;
    @Autowired
    private HttpServletRequest request;

    /**
     * 用户登录
     * @param username
     * @param password
     * @return
     */
    @PostMapping("/login")
    public Result login(String username,String password){
        //实现登录
        AuthToken authToken = userService.login(username, password, clientId, clientSecret);
        //将用户的token令牌放入到cookie中去
        //CookieUtil.addCookie(response,cookieDomain,"/","Authorization",authToken.getAccessToken()
        //    ,cookieMaxAge,true);
        CookieTools.setCookie(request,response,"Authorization",authToken.getAccessToken());
        //将用户的用户名存入cookie
        CookieTools.setCookie(request,response,"cuname",username);
        return new Result(true, StatusCode.OK,"登录成功");
    }
}
