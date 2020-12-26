package com.changgou.search.service;

import java.util.Map;

public interface SearchService {
    /**
     * 商品的搜索
     * @param searchMap
     * @return
     */
    //将要搜索的关键词所有的查询条件 用一个map来放  然后返回一个map  里面包含多种数据
    public Map<String, Object> search(Map<String,String> searchMap);
    /**
     * 导入数据
     */
    public void importSku();
}
