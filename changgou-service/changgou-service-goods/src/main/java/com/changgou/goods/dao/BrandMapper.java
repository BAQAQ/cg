package com.changgou.goods.dao;
import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:itheima
 * @Description:Brand的Dao
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     * 根据类别id查询商标信息
     * @param categoryid
     * @return
     */
    @Select("select a.* from tb_brand a,tb_category_brand b where a.id=b.brand_id and b.category_id=#{categoryid}")
    List<Brand> findByCategoryId(@Param("categoryid") Integer categoryid);
}
