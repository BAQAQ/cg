package com.changgou.searchweb.controller;

import com.changgou.search.feign.SearchFeign;
import com.changgou.util.Page;
import com.changgou.util.Result;
import com.changgou.util.UrlUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private SearchFeign searchFeign;
    /**
     * 搜索页面后台
     * @return
     */
    @RequestMapping("/list")
    public String search(@RequestParam Map<String,String> searchMap, Model model){
        //远程调用搜索微服务 获取查询的结果
        Result<Map> result= searchFeign.search(searchMap);
        Map<String, Object> searchResult = result.getData();
        //查询到的数据放入到视图中去
        model.addAttribute("result",searchResult);
        //查询条件回显使用
        model.addAttribute("searchMap",searchMap);
        //获取搜索的url
        String url = getUrl(searchMap);
        model.addAttribute("url",url);
        //添加的
        String sortUrl = UrlUtils.replateUrlParameter(url, "sortField", "sortRule");
        model.addAttribute("sortUrl",sortUrl);
        //分页
        //获取总条数
        int totalElements = Integer.parseInt(searchResult.get("totalElements").toString());
        //获取当前页码
        int pageNumber = Integer.parseInt(searchResult.get("pageNumber").toString());
        //获取每页显示多少条
        int pageSize = Integer.parseInt(searchResult.get("pageSize").toString());
        //获取分页对象
        Page<Object> page = new Page<>(totalElements, pageNumber, pageSize);
        model.addAttribute("page",page);//分页
        return "search";
    }

    /**
     * 拼接url
     * @param searchMap
     * @return
     */
    private static String getUrl(Map<String,String> searchMap){
        String url="/search/list?";
        //对所以的参数进行循环
        for (Map.Entry<String, String> entry : searchMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //http://localhost:18086/search/list?keywords=华为&
            url+=key+"="+value+"&";
        }
        return url.substring(0,url.length()-1);//从0开始到倒数第二位

    }

   /* *//**
     * 测试方法
     * @param args
     *//*
    public static void main(String[] args) {
        Map<String,String> searchMap=new HashMap<>();
        searchMap.put("keywords","华为");
        searchMap.put("category","手机");
        searchMap.put("brand","朵唯");
        String url = getUrl(searchMap);
        System.out.println(url);
        //search/list?keywords=华为&category=手机&brand=朵唯&
        //多了一个&
    }*/
}
