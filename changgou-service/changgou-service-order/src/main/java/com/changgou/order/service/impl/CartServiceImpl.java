package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private SpuFeign spuFeign;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增购物车
     *
     * @param num
     * @param skuId
     */
    @Override
    public void addCart(Integer num, String skuId,String username) {
        //订单
        OrderItem orderItem=new OrderItem();
        //判断数据
        if( StringUtils.isEmpty(skuId)){
            throw new RuntimeException("参数错误！！");
        }
        //当购物车商品小于等于0时候 移除商品
        if(num<=0){
            redisTemplate.boundHashOps("Cart_"+username).delete(skuId);
            return;
        }

        //通过skuid查询sku的信息
        Sku sku = skuFeign.findById(skuId).getData();
        if (sku==null||sku.getId()==null){
            throw new RuntimeException("商品不存在，不能加入购物车");
        }
        //商品名字
        orderItem.setName(sku.getName());
        //商品价格
        orderItem.setPrice(sku.getPrice());
        //总价格
        orderItem.setMoney(sku.getPrice()*num);
        //购买的商品数量
        orderItem.setNum(num);
        orderItem.setSkuId(sku.getId());
        orderItem.setImage(sku.getImage());
        orderItem.setSpuId(sku.getSpuId());
        //获取spu详情
        Spu spu = spuFeign.findById(sku.getSpuId()).getData();
        if(spu==null||spu.getId()==null){
            throw new RuntimeException("商品spu不存在，不能加入购物车");
        }
        //123级分类
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        //将数据保存到redis中去
        redisTemplate.boundHashOps("Cart_"+username).put(sku.getId(),orderItem);
    }

    /**
     * 查询购物车信息
     *
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> getCart(String username) {
        List<OrderItem> orderItemList = redisTemplate.boundHashOps("Cart_" + username).values();
        return orderItemList;
    }

    /**
     * 查询购物车信息
     *
     * @param username
     * @param ids
     * @return
     */
    @Override
    public List<OrderItem> choose(String username, String[] ids) {
        List<OrderItem> orderItems=new ArrayList<>();
        for (String id : ids) {
            OrderItem orderItem =(OrderItem)redisTemplate.boundHashOps("Cart_" + username).get(id);
            orderItems.add(orderItem);
        }
        return orderItems;
    }
}
