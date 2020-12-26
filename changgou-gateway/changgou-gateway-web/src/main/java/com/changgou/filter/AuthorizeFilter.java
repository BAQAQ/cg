package com.changgou.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {


    /**
     * z自定义过滤方法
     * @param exchange
     * @param chain
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        //判断用户请求的地址 是不是登录地址  如果是 直接放行
        String path = request.getURI().getPath();
        if(path.startsWith("/api/user/login")){
            return chain.filter(exchange);
        }
        //从url中获取token
        //token为空 则从header中获取token
        String token = request.getQueryParams().getFirst("Authorization");
        if(StringUtils.isEmpty(token)){
            //头中获取
            token = request.getHeaders().getFirst("Authorization");
            //从cookie中获取令牌
            if(StringUtils.isEmpty(token)){
                token = request.getCookies().getFirst("Authorization").getValue();
            }

        }
        //token还是为空 直接拒绝
        if(StringUtils.isEmpty(token)){
           response.setStatusCode(HttpStatus.UNAUTHORIZED);
           return response.setComplete();
        }
        //token不为空 放入请求头中去
        request.mutate().header("Authorization","Bearer "+token);
        return chain.filter(exchange);
    }

    /**
     * 过滤器执行顺序
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
