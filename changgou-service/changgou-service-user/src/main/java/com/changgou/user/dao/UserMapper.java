package com.changgou.user.dao;
import com.changgou.user.pojo.User;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:itheima
 * @Description:User的Dao
 *****/
public interface UserMapper extends Mapper<User> {
    /**
     * 增加用户积分
     * @param point
     * @param username
     */
    @Update("update tb_user set points=points+#{point} where username=#{username}")
    void addPoint(String username,Integer point);
}
