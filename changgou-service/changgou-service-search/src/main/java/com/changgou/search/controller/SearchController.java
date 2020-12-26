package com.changgou.search.controller;

import com.changgou.search.service.SearchService;
import com.changgou.util.Result;
import com.changgou.util.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin
public class SearchController {
    @Autowired
    private SearchService searchService;

    /**
     * 商品搜索
     *
     */
    @GetMapping
    public Result<Map> search(@RequestParam(required = false) Map<String,String> searchMap){
        Map<String, Object> stringObjectMap = searchService.search(searchMap);
        return  new Result<>(true,StatusCode.OK,"商品搜索成功",stringObjectMap);
    }

    /**
     * 导入数据
     * @return
     */
    @GetMapping("import")
    public Result importData(){
        searchService.importSku();
        return new Result(true, StatusCode.OK,"导入数据成功");
    }
}
