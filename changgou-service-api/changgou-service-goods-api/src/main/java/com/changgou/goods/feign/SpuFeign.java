package com.changgou.goods.feign;

import com.changgou.goods.pojo.Spu;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "goods")
public interface SpuFeign {


    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public Result<Spu> findById(@PathVariable(value = "id") String id);

}
