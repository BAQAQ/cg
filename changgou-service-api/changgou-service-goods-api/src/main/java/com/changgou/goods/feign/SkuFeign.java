package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name ="goods" )
@RequestMapping("sku")
public interface SkuFeign {
    /**
     * 根据审核状态查询商品信息
     *
     * @param status
     * @return
     */
    @GetMapping("status/{status}")
    public Result<List<Sku>> findByStatus(@PathVariable(value = "status") String status);

    /***
     * 根据ID查询Sku数据
     * @param id
     * @return
     */
   @GetMapping("/{id}")
    public Result<Sku> findById(@PathVariable(value = "id") String id);

    /**
     * 扣减商品库存
     * @return
     */
    @GetMapping("/decount")
    public  Result decount(@RequestParam("map") Map<String,Object> map);

    }
