package com.changgou.content.feign;

import com.changgou.content.pojo.Content;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * 广告微服务远程调用
 */
@FeignClient(name = "content")
public interface ContentFeign {
    /**
     * 通过类别id查询广告列表
     *
     * @param cid
     * @return
     */
    @GetMapping("content/category/{cid}")
    public Result<List<Content>> findByCategoryId(@PathVariable(value = "cid") Integer cid);


}
