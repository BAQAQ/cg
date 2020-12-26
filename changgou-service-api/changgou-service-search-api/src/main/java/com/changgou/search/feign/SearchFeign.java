package com.changgou.search.feign;

import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name="search")
public interface SearchFeign {

    /**
     * 商品搜索
     *
     */
    @GetMapping("search")
    public Result<Map> search(@RequestParam(required = false) Map<String,String> searchMap);

}
