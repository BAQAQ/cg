package com.changgou.goods.service;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;
import java.util.List;
/****
 * @Author:itheima
 * @Description:Spu业务层接口
 *****/
public interface SpuService {
    /**
     *
     *商品逻辑删除
     */
    void deleteLogical(Long spuId);
    /**
     * 商品批量上架
     * @param ids
     * @return
     */
    int putMany(Long[] ids);
    /***
     * 商品上架
     * @param spuId
     */
    void put(Long spuId);
    /***
     * 商品下架
     * @param spuId
     */
    void pull(Long spuId);
    /***
     * 商品审核
     * @param spuId
     */
    void audit(Long spuId);
    /**
     * 新增商品：spu+sku
     * @param goods
     */
    public void save(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(String id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(String id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
