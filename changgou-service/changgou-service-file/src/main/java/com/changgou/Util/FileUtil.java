package com.changgou.Util;

import com.changgou.pojo.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

//文件的工具类
public class FileUtil {

    //初始化tracker信息
    static{
        try {
            //加载路径下的指定配置文件
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //加载到fastdfs中去 完成初始化
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件的服务
     * @param fastDFSFile
     * @return
     */
    public static String[] fileUpload(FastDFSFile fastDFSFile){
        NameValuePair[] meta_list = new NameValuePair[2];//定义并初始化长度为2的数组
        meta_list[0]=new NameValuePair("name","sdd");
        meta_list[1]=new NameValuePair("location","fff");
        try {
            //声明一个trackerclient
            TrackerClient trackerClient = new TrackerClient();
            //获取连接 获得trackerserver
            TrackerServer trackerServer = trackerClient.getConnection();
            //storage
            StorageClient storageClient=new StorageClient(trackerServer,null);
            //文件上传
            String[] string = storageClient.upload_file(fastDFSFile.getContent(),
                fastDFSFile.getExt(), meta_list);
            return  string;
        } catch (Exception e) {
            e.printStackTrace();
            //出异常 返回空
            return null;
        }

    }

    /***
     * 获取文件信息
     * @param groupName:组名
     * @param remoteFileName：文件存储完整名
     */
    public static FileInfo getFile(String groupName, String remoteFileName){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获得TrackerServer信息
            TrackerServer trackerServer =trackerClient.getConnection();
            //通过TrackerServer获取StorageClient对象
            StorageClient storageClient = new StorageClient(trackerServer,null);
            //获取文件信息
            return storageClient.get_file_info(groupName,remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 下载文件
     * @param groupName :组名
     * @param remoteFileName ：文件存储完整名
     */
    public static InputStream download(String groupName, String remoteFileName){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获得TrackerServer信息
            TrackerServer trackerServer =trackerClient.getConnection();
            //通过TrackerServer获取StorageClient对象
            StorageClient storageClient = new StorageClient(trackerServer,null);
            //下载文件
            byte[] bytes = storageClient.download_file(groupName, remoteFileName);
            //把字节码换成输入流
            //ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /***
     * 删除文件
     * @param groupName :组名
     * @param remoteFileName ：文件存储完整名
     */
    public static void delete(String groupName, String remoteFileName){
        try {
            //创建TrackerClient对象
            TrackerClient trackerClient = new TrackerClient();
            //通过TrackerClient获得TrackerServer信息
            TrackerServer trackerServer =trackerClient.getConnection();
            //通过TrackerServer获取StorageClient对象
            StorageClient storageClient = new StorageClient(trackerServer,null);
            //下载文件
            int i = storageClient.delete_file(groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws Exception{
        //文件上传测试
        //FileInfo fileInfo=getFile("group1","M00/00/00/wKjThF-6QW-ASvqnAACYcOPdDY0978.jpg");
        //System.out.println(fileInfo);


        //文件下载测试
        //InputStream inputStream=download("group1","M00/00/00/wKjThF-6QW-ASvqnAACYcOPdDY0978.jpg");
        ////输出流
        //FileOutputStream fileOutputStream = new FileOutputStream(new File("g:/dfg/cc.bmp"));
        //byte[] buffer = new byte[1024];
        //int len=0;
        ////读取文件
        //while ((len=inputStream.read(buffer))!=-1){
        //    //写入
        //    fileOutputStream.write(len);
        //}
        ////关闭流
        //inputStream.close();
        //fileOutputStream.close();

        //文件删除
        delete("group1","M00/00/00/wKjThF-6QW-ASvqnAACYcOPdDY0978.jpg");

    }




}
