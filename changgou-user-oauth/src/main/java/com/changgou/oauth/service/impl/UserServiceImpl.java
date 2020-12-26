package com.changgou.oauth.service.impl;

import com.changgou.oauth.pojo.AuthToken;
import com.changgou.oauth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private LoadBalancerClient loadBalancerClient;

    /**
     * 用户登录
     *
     * @param username     用户
     * @param password
     * @param clientId     客户端
     * @param clientSecret
     * @return
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        ServiceInstance choose = loadBalancerClient.choose("user-auth");
        String url = choose.getUri().toString();//地址：端口号

        //请求的url
       url=url+"/oauth/token";
        try {
            //定义head
            MultiValueMap<String,String> headers=new LinkedMultiValueMap<>();
            String header = getHeader(clientId, clientSecret);
            //定义head
            headers.set("Authorization",header);
            //定义body
            MultiValueMap<String,String> body=new LinkedMultiValueMap<>();
            //设置模式为密码模式
            body.set("grant_type","password");
            body.set("username",username);
            body.set("password",password);
            ResponseEntity<Map> exchange = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            //获取返回值
            Map<String, String> result = exchange.getBody();
            AuthToken authToken = new AuthToken();
            //获取令牌
            String access_token = result.get("access_token");
            authToken.setAccessToken(access_token);
            //获取刷新的令牌
            String refresh_token = result.get("refresh_token");
            authToken.setRefreshToken(refresh_token);
            //获取jti
            String jti = result.get("jti");
            authToken.setJti(jti);
            return authToken;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private String getHeader(String clientId,String clientSecret) throws Exception{
        String head=clientId+":"+clientSecret;
        //base64加密
        byte[] encode = Base64.getEncoder().encode(head.getBytes());
        return "Basic " + new String(encode,"UTF-8");
    }

  /*  public static void main(String[] args)  throws Exception{
        String a="Y2hhbmdnb3U6Y2hhbmdnb3U=";
        byte[] decode = Base64.getDecoder().decode(a);
        String s = new String(decode, "UTF-8");
        System.out.println(s);
    }*/
}
