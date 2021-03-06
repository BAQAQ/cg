package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import com.changgou.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name="user")
public interface UserFeign {

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping("/user/{id}")
    public Result<User> findById(@PathVariable(value = "id") String id);

    /**
     * 新增积分
     * @param point
     * @return
     */
    @GetMapping("/user/addPoint")
    public Result addPoint(@RequestParam("point") Integer point);
}
