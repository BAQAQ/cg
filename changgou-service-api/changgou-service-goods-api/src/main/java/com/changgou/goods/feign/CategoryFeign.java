package com.changgou.goods.feign;

import com.changgou.goods.pojo.Category;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "goods")
public interface CategoryFeign {

    /***
     * 根据ID查询Category数据
     * @param id
     * @return
     */
    @GetMapping("/category/{id}")
    public Result<Category> findById(@PathVariable(value = "id") Integer id);

}


