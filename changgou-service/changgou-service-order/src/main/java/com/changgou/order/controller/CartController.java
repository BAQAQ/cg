package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import com.changgou.order.util.TokenDecode;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping("/add")
    public Result addCart(String skuId,Integer num){
        //String username="sunwukong";
        //动态获取用户数据
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        cartService.addCart(num,skuId,username);
        return  new Result(true, StatusCode.OK,"新增购物车成功");

    }

    @GetMapping("/list")
    public Result<List<OrderItem>> getCart(){
        //String username="sunwukong";
        //动态获取用户数据
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> cart = cartService.getCart(username);
        return  new Result(true, StatusCode.OK,"获取购物车数据成功",cart);

    }

    /***
     * 查询选中的购物车商品
     * @param ids
     * @return
     */
    @GetMapping("/list/choose")
    public Result<List<OrderItem>> getCart(@RequestParam("ids") String[] ids){
        //String username="sunwukong";
        //动态获取用户数据
        Map<String, String> userInfo = TokenDecode.getUserInfo();
        String username = userInfo.get("username");
        List<OrderItem> cart = cartService.choose(username,ids);
        return  new Result(true, StatusCode.OK,"查询选中的购物车商品数据成功",cart);
    }

}
