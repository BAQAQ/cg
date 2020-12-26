package com.changgou.test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class Test {

    @org.junit.Test
    public void creatJWT() throws Exception{
        JwtBuilder jwtBuilder = Jwts.builder()
            .setId("1533")
            .setSubject("ccccf")//描述说明
            //.setExpiration(new Date(System.currentTimeMillis()+10000))//设置当前时间得10秒钟后过期
            .signWith(SignatureAlgorithm.HS256, "kshgr");//算法格式  盐
        String compact = jwtBuilder.compact();
        System.out.println(compact);

    }

    /**
     * 解析jwt
     */
    @org.junit.Test
    public void JX(){
        Claims body = Jwts.parser()
            .setSigningKey("kshgr").parseClaimsJws("eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIxNTMzIiwic3ViIjoiY2NjY2YifQ.S6CVl9kOi-iqjHa1HJI5sKXXKSxpNZEi2MfuG8bBLrk")
            .getBody();//获得载荷
        System.out.println(body);
    }
}
