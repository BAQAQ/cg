package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SearchDao;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import com.changgou.util.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private SearchDao searchDao;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 商品的搜索
     *
     * @param searchMap
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //构建查询条件 1.搜索where后面的条件 2.类别 品牌 规格的group by条件也设置好了 3.高亮条件
        NativeSearchQueryBuilder builder = creatBuild(searchMap);
        /**
         * 查询的开始------start------
         */
        //执行查询  关键字组合条件查询结果获取  聚合条件查询结果获取
        Map<String, Object> searchResult = getSearchResult(builder,searchMap);
        /**
         * 查询的结束------end------
         */
        //返回结果
        return searchResult;
    }
    /**
     * 提取聚合结果
     *
     */
    private List<String> getResult(Aggregations aggregations,String termName){
        //返回list集合类型
        List<String> list=new ArrayList<>();
        //获取聚合的结果
        StringTerms stringTerms = aggregations.get(termName);
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            //加入到list中
            list.add(keyAsString);
        }
        return list;
    }

    /**
     * 分类信息的聚合查询
     * @param builder
     * @return
     */
    private List<String> getCategoryList(NativeSearchQueryBuilder builder) {
        //返回list集合类型
        List<String> categoryList=new ArrayList<>();
        //指定要对哪个域进行聚合==group by  terms(取别名）  field(指定聚合的域 即需要去重的域） size（es默认查询一千条，这里改大一点）
        //指定对那个域聚合查询 聚合查询条数   聚合查询之后取别名
        builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取聚合的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("category");
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            //加入到list中
            categoryList.add(keyAsString);
        }
        return categoryList;
    }

    /**
     * 解析规格的字符串 去重复
     * @param spec
     * @return
     */
    private Map<String, Set<String>> getSpecInfo(Map<String, Set<String>> specInfo,String spec){//这里spec是单个
        //如果为空 则新建一个
        if(specInfo==null||specInfo.size()==0){
            specInfo=new HashMap<>();
        }
        //将string类型转换为Map
        Map<String, String> map = JSONObject.parseObject(spec, Map.class);
        //对Map循环获取key-value
        for (Map.Entry<String, String> entry : map.entrySet()) {
            //获取单条数据的key eg:电视网络
            String key = entry.getKey();
            //获取对应的value 机顶盒
            String value = entry.getValue();
            //通过key获取value value是一个set
            //拿到set
            Set<String> set = specInfo.get(key);
            //判断set
            if(set==null||set.size()==0){
                //为空 或者长度为00则说明是首次创建 则需要new一个 初始化
                set=new HashSet<>();
            }
            //添加值到set里面
            set.add(value);
            //设置规格属性
            specInfo.put(key, set);
        }
        return specInfo;

    }
    /**
     * 执行查询 获取返回的结果
     * 关键字的返回结果和聚合的返回结果
     * @param builder
     * @return
     */
    private  Map<String, Object> getSearchResult(NativeSearchQueryBuilder builder,Map<String, String> searchMap){
        //定义返回结果
        Map<String, Object> result=new HashMap<>();
        //执行查询 分页查询结果
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(),
            SkuInfo.class, new SearchResultMapper() {
                /**
                 * 后置处理查询到的结果
                 * @param searchResponse
                 * @param aClass
                 * @param pageable
                 * @param <T>
                 * @return
                 */
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                    List<T> skuInfoList=new ArrayList<>();
                    //进入现在这个方法
                    SearchHits hits = searchResponse.getHits();
                    //迭代器
                    Iterator<SearchHit> iterator = hits.iterator();
                    //循环
                    while(iterator.hasNext()){
                        //获取每一条数据
                        SearchHit next = iterator.next();
                        String sourceAsString = next.getSourceAsString();
                        //转换数据类型 现在获取的对象是不包含高亮数据
                        SkuInfo skuInfo = JSONObject.parseObject(sourceAsString, SkuInfo.class);
                        //获取高亮数据
                        HighlightField highlightField = next.getHighlightFields().get("name");
                        Text[] fragments = highlightField.fragments();
                        String name="";
                        //高亮数据遍历循环
                        for (Text fragment : fragments) {
                            name+=fragment;
                        }
                        //替换旧的数据
                        skuInfo.setName(name);
                        skuInfoList.add((T)skuInfo);
                    }
                    Aggregations aggregations = searchResponse.getAggregations();
                    //现在返回的结果是es查询到的结果自定义处理后，new出来的新的结果集
                    return new AggregatedPageImpl<T>(skuInfoList,pageable,hits.getTotalHits(),aggregations);
                }
            });
        //获取需要的List<skuinfo>
        List<SkuInfo> content = skuInfos.getContent();
        //返回到页面去 关键字查询结果
        result.put("rows",content);


        //聚合条件结果获取----------开始---
        //获取所有的聚合结果
        Aggregations aggregations = skuInfos.getAggregations();
        String category = searchMap.get("category");
        //获取聚合的结果 当类别不是查询条件的时候
        if(StringUtils.isEmpty(category)){
            List<String> categoryList=getResult(aggregations, "category");
            result.put("categoryList",categoryList);
        }
        //获取聚合的结果 当品牌不是查询条件的时候
        String brand = searchMap.get("brand");
        if(StringUtils.isEmpty(brand)){
            List<String> brandList=getResult(aggregations, "brand");
            result.put("brandList",brandList);
        }
        //获取规格的聚合结果
        List<String> specList = getResult(aggregations, "spec");
        //定义返回的结果集
        Map<String, Set<String>> specInfo=null;
        for (String spec : specList) {
            //调用去重方法
            specInfo = getSpecInfo(specInfo, spec);
        }
        result.put("specList",specInfo);
        //---------结束

        return result;
    }
    /**
     * 构建搜索的条件
     * 构建聚合的条件
     * @param searchMap
     * @return
     */
    private NativeSearchQueryBuilder creatBuild(Map<String, String> searchMap){
        //构件查询
        NativeSearchQueryBuilder builder=new NativeSearchQueryBuilder();
        //bool条件构造器 must not should
        BoolQueryBuilder boolQueryBuilder=new BoolQueryBuilder();
        //定义高亮查询条件
        HighlightBuilder.Field field=new HighlightBuilder.Field("name")//指定高亮域
                                                .preTags("<font style='color:red'>")//前缀
                                                .postTags("</font>")//后缀
                                                .fragmentSize(100);//查询前100字
        //设置高亮的查询条件
        builder.withHighlightFields(field);
        //判断查询条件是否为空
        if(searchMap!=null){
            //关键词搜索
            String keywords = searchMap.get("keywords");
            if(!StringUtils.isEmpty(keywords)){
                //构建查询条件
                boolQueryBuilder.must(QueryBuilders.matchQuery("name",keywords));
            }
            //类别的名字
            String category = searchMap.get("category");
            if(!StringUtils.isEmpty(category)){
                //构建查询条件
                boolQueryBuilder.must(QueryBuilders.termQuery("categoryName",category));
            }
            //品牌名字
            String brand = searchMap.get("brand");
            if(!StringUtils.isEmpty(brand)){
                //构建查询条件
                boolQueryBuilder.must(QueryBuilders.termQuery("brandName",brand));
            }
            //规格数据判断
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                //获取查询条件参数名字spec_网络格式
                String key = entry.getKey();
                //判断
                if(key.startsWith("spec_")){
                    String value = entry.getValue();
                    //将spec_网络格式替换成网络格式
                    key = key.replace("spec_", "");
                    //构建查询条件"specMap."+key+".keyword" 来自kibana 没有分词的域 可聚合的specMap.网络格式.keyword
                    boolQueryBuilder.must(QueryBuilders.termQuery("specMap."+key+".keyword",value));
                }
            }
            //价格的处理100-1000元  1000以上
            String price = searchMap.get("price");
            if(!StringUtils.isEmpty(price)){
                //100-1000  1000
                price=price.replace("元","").replace("以上","");
                //100 1000
                String[] split = price.split("-");
                boolQueryBuilder.must(QueryBuilders.rangeQuery("price").gt(split[0]));//大于100
                if(split.length>1){
                    boolQueryBuilder.must(QueryBuilders.rangeQuery("price").lte(split[1]));//小于等于1000
                }
            }
            //先用bool条件构造器将所有条件关联起来 最后放入到查询条件中去
            builder.withQuery(boolQueryBuilder);
        }
        //分页条件设置
        Integer page = getPage(searchMap.get("page"));//页数
        int size=20;//每页显示多少条
        //初始化分页信息 第一页对应的page(0)
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        //设置分页查询条件
        builder.withPageable(pageRequest);
        //排序实现
        //排序的规则 asc  deac
        String sortRule = searchMap.get("sortRule");
        //设置排序的域
        String sortField = searchMap.get("sortField");
        if(!StringUtils.isEmpty(sortRule)&&!StringUtils.isEmpty(sortField)){
            builder.withSort(SortBuilders.fieldSort(sortField).order(SortOrder.valueOf(sortRule)));
        }
        //聚合条件构建-----------开始----
        //类别
        String category = searchMap.get("category");
        if(StringUtils.isEmpty(category)){
            builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        }
        //品牌
        String brand = searchMap.get("brand");
        if(StringUtils.isEmpty(brand)){
            builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        }
        //规格
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));
        //----------------结束----------
        return builder;
    }

    /**
     * 计算用户想看第几页
     * @param page
     * @return
     */
    private Integer getPage(String page){
        try {
            //转化指定页数
            int i = Integer.parseInt(page);
            return i;
        } catch (NumberFormatException e) {
            //转化失败 返回第一页
           return 1;
        }
    }

    /**
     * 导入数据
     */
    @Override
    public void importSku() {
        //远程调用商品微服务查询商品List<Sku>
        Result<List<Sku>> result = skuFeign.findByStatus("1");
        if(!result.isFlag()){
            throw new RuntimeException("远程调用商品微服务失败");
        }
        List<Sku> skuList = result.getData();
        //将Sku转换为Skuinfo
        List<SkuInfo> skuInfoList = JSONObject.parseArray(JSONObject.toJSONString(skuList),
            SkuInfo.class);
        //将skuinfo中的string类型的spec字符串转换为specMap
        for (SkuInfo skuInfo : skuInfoList) {
            //获得字符串类型规格信息
            String spec = skuInfo.getSpec();
            //将字符串转map
            Map<String,Object> specMap = JSONObject.parseObject(spec, Map.class);
           //存入skuinfo
            skuInfo.setSpecMap(specMap);
            //保存数据
            searchDao.save(skuInfo);
        }
            //保存数据
        searchDao.saveAll(skuInfoList);
    }

    //整合完毕之后 未用到的灰色方法 可以删除-------------------------------------------------

    /**
     * 规格信息的聚合查询
     * @param builder
     * @return
     */
    private  Map<String, Set<String>> getSpecList(NativeSearchQueryBuilder builder) {
        //定义返回的结果集
        Map<String, Set<String>> specInfo=null;
        //指定要对哪个域进行聚合==group by  terms(取别名）  field(指定聚合的域 即需要去重的域） size（es默认查询一千条，这里改大一点）
        //这里的别名根据kibana查看
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取聚合的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("spec");
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            //获取到spec的单条数据 eg：{"电视网络":"机顶盒","电视音响效果":"小影院","电视屏幕尺寸":"75英寸","电视类型":"人工智能"}
            String keyAsString = bucket.getKeyAsString();
            specInfo = getSpecInfo(specInfo,keyAsString);
        }
        return specInfo;
    }

    /**
     * 品牌信息的聚合查询
     * @param builder
     * @return
     */
    private List<String> getBrandList(NativeSearchQueryBuilder builder) {
        //返回list集合类型
        List<String> categoryList=new ArrayList<>();
        //指定要对哪个域进行聚合==group by  terms(取别名）  field(指定聚合的域 即需要去重的域） size（es默认查询一千条，这里改大一点）
        //指定对那个域聚合查询 聚合查询条数   聚合查询之后取别名
        builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);
        //获取聚合的结果
        StringTerms stringTerms = skuInfos.getAggregations().get("brand");
        for (StringTerms.Bucket bucket : stringTerms.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            //加入到list中
            categoryList.add(keyAsString);
        }
        return categoryList;
    }

    /**
     * 获取聚合查询的结果
     * @param builder
     * @param searchMap
     * @return
     */
    private Map<String, Object> getAggResult(NativeSearchQueryBuilder builder,
                                             Map<String,String> searchMap){
        Map<String, Object> map=new HashMap<>();
        //类别
        String category = searchMap.get("category");
        if(StringUtils.isEmpty(category)){
            builder.addAggregation(AggregationBuilders.terms("category").field("categoryName").size(100000));
        }
        //品牌
        String brand = searchMap.get("brand");
        if(StringUtils.isEmpty(brand)){
            builder.addAggregation(AggregationBuilders.terms("brand").field("brandName").size(100000));
        }
        //规格
        builder.addAggregation(AggregationBuilders.terms("spec").field("spec.keyword").size(100000));

        //执行查询
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(builder.build(), SkuInfo.class);

        //获取所有的聚合结果
        Aggregations aggregations = skuInfos.getAggregations();
        //获取聚合的结果 当类别不是查询条件的时候
        if(StringUtils.isEmpty(category)){
            List<String> categoryList=getResult(aggregations, "category");
            map.put("categoryList",categoryList);
        }
        //获取聚合的结果 当品牌不是查询条件的时候
        if(StringUtils.isEmpty(brand)){
            List<String> brandList=getResult(aggregations, "brand");
            map.put("brandList",brandList);
        }
        //获取规格的聚合结果
        List<String> specList = getResult(aggregations, "spec");
        //定义返回的结果集
        Map<String, Set<String>> specInfo=null;
        for (String spec : specList) {
            //调用去重方法
            specInfo = getSpecInfo(specInfo, spec);
        }
        map.put("specList",specInfo);

        return map;

    }
}
