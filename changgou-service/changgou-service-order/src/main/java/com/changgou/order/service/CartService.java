package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

/**
 * 购物车接口
 */
public interface CartService {
    /**
     * 新增购物车
     * @param num
     * @param skuId
     */
    void  addCart(Integer num,String skuId,String username);

    /**
     * 查询购物车信息
     * @param username
     * @return
     */
    List<OrderItem> getCart(String username);

    /**
     * 查询购物车信息
     * @param username
     * @return
     */
    List<OrderItem> choose(String username,String[] ids);
}
