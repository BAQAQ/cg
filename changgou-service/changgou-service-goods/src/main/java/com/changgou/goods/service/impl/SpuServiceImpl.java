package com.changgou.goods.service.impl;
import com.alibaba.fastjson.JSONObject;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.*;
import com.changgou.goods.service.SpuService;
import com.changgou.util.IdWorker;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

/****
 * @Author:itheima
 * @Description:Spu业务层接口实现类
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SkuMapper skuMapper;


    //查询品牌信息

    /**
     * 商品逻辑删除
     *
     * @param spuId
     */
    @Override
    public void deleteLogical(Long spuId) {
        //逻辑删除 商品状态为下架 未审核
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if(spu.getIsMarketable().equals("1")){
            //抛异常
            throw new RuntimeException("商品处于上架状态，无法被逻辑删除");
        }
        if(spu.getStatus().equals("0")){
            //抛异常
            throw new RuntimeException("商品未审核通过");
        }

    }

    /**
     * 商品批量上架
     *
     * @param ids
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        return 0;
    }

    /***
     * 商品上架
     * @param spuId
     */
    @Override
    public void put(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否已经被删除
        if(spu.getIsDelete().equals("1")){
            //抛异常
            throw new RuntimeException("商品已经被删除，无法上架");
        }
        if(spu.getStatus().equals("0")){
            //抛异常
            throw new RuntimeException("未审核的商品无法上架");
        }
        spu.setIsMarketable("1");//设置状态为上架
        //更新
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /***
     * 商品下架
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断商品是否已经被删除
        if(spu.getIsDelete().equals("1")){
            //抛异常
            throw new RuntimeException("商品已经被删除，无法下架");
        }
        spu.setIsMarketable("0");//设置状态为下架
        //更新
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /***
     * 商品审核
     * @param spuId
     */
    @Override
    public void audit(Long spuId) {
        //审核商品，需要校验是否是被删除的商品，如果未删除则修改审核状态为1，并自动上架
        //查询商品
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //下架商品，需要校验是否是被删除的商品，如果未删除则修改上架状态为0
        //判断商品是否已经被删除了  数据库中1状态号代表已删除
        if(spu.getIsDelete().equals("1")){
            //抛异常
            throw new RuntimeException("商品已经被删除，无法审核");
        }
        //商品没有被删除 修改商品状态 改为上架
        //上架商品，需要审核通过的商品
        spu.setStatus("1");//已审核
        spu.setIsMarketable("1");//已上架
        //修改
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 新增商品：spu+sku
     *
     * @param goods
     */
    @Override
    public void save(Goods goods) {
        //获取spu
        Spu spu = goods.getSpu();
        //获取sku
        List<Sku> skuList = goods.getSkuList();
        //补全spu需要的的一些参数
        spu.setId("Spu"+ idWorker.nextId());
        //保存spu
        spuMapper.insertSelective(spu);
        //查询品牌信息
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        //查询类别信息
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //补全sku需要的一些参数
        for (Sku sku : skuList) {
            //补全id
            sku.setId("Sku"+idWorker.nextId());
            //name 名字是拼接的 拼接sku的参数值
            String name = spu.getName();
            //获取sku的规格信息
            String spec = sku.getSpec();
            Map<String,String> map = JSONObject.parseObject(spec, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = entry.getValue();
                name=name+""+value;
            }
            sku.setName(name);

            //创建time
            sku.setCreateTime(new Date());
            //修改时间
            sku.setUpdateTime(new Date());
            //spuid
            sku.setSpuId(spu.getId());
            //categoryid
            sku.setCategoryId(spu.getCategory3Id());
            //类别的名字
            sku.setCategoryName(category.getName());
            //品牌的名字
            sku.setBrandName(brand.getName());
            skuMapper.insertSelective(sku);
        }
        //保存sku
    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(String id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
