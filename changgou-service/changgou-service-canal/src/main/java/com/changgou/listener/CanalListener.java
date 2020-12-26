package com.changgou.listener;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.changgou.util.Result;
import com.xpand.starter.canal.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalListener {

    @Autowired
    private ContentFeign contentFeign;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 监听新增类型的消息
     */
    @InsertListenPoint
    public  void insertEvent(CanalEntry.RowData rowData){
        List<CanalEntry.Column> columnList = rowData.getAfterColumnsList();
            syncFromDatabaseToRedis(columnList);
    }

    /**
     * 监听修改类型的消息
     */
    @UpdateListenPoint
    public  void updateEvent(CanalEntry.RowData rowData){
        //修改前数据  一个数据移动了位置 需要查移动前所在地方的数据变化
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        syncFromDatabaseToRedis(beforeColumnsList);

        //修改后  移动到现在所在位置 需要查询现在所在位置数据变化
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        syncFromDatabaseToRedis(afterColumnsList);
    }

    /**
     * 监听删除类型的消息
     */
    @DeleteListenPoint
    public  void deleteEvent(CanalEntry.RowData rowData){
        List<CanalEntry.Column> beforeColumnsList = rowData.getAfterColumnsList();
            syncFromDatabaseToRedis(beforeColumnsList);
    }

    /**
     * 监听自定义类型的消息
     */
    @ListenPoint(destination="example",
        schema = {"changgou_content"},//监控的数据库名字
        table = "tb_content",//监控的表名
        eventType={CanalEntry.EventType.UPDATE
        ,CanalEntry.EventType.DELETE,CanalEntry.EventType.INSERT})//监控事件类型
    public  void myEvent(CanalEntry.EventType eventType,CanalEntry.RowData rowData){
        //修改前数据  一个数据移动了位置 需要查移动前所在地方的数据变化
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        if(beforeColumnsList!=null&&beforeColumnsList.size()>0){
            syncFromDatabaseToRedis(beforeColumnsList);
        }

        //修改后  移动到现在所在位置 需要查询现在所在位置数据变化
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        if(afterColumnsList!=null&&afterColumnsList.size()>0){
            syncFromDatabaseToRedis(afterColumnsList);
        }

    }

    /**
     *
     * @param colums
     */

    private void syncFromDatabaseToRedis(List<CanalEntry.Column> colums){
        for (CanalEntry.Column column : colums) {
            String name = column.getName();
            String value = column.getValue();
            if(name.equals("category_id")){
                //新增后的指定广告位的所有广告列表
                Result<List<Content>> result = contentFeign.findByCategoryId(Integer.valueOf(value));
                if(result.isFlag()){
                    //获取查询到的广告列表
                    List<Content> contents = result.getData();
                    //放入redis中去
                    stringRedisTemplate.boundValueOps("content"+value).set(JSONObject.toJSONString(contents));
                }
            }
        }

    }

}

