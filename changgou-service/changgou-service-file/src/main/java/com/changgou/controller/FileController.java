package com.changgou.controller;



import com.changgou.Util.FileUtil;
import com.changgou.pojo.FastDFSFile;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin  //开启跨域访问
public class FileController {

    /**
     * 文件上传
     * @param file
     * @return
     * @throws Exception
     */
    @PostMapping("upload")
    public String fileUpload(@RequestParam("file") MultipartFile file)throws Exception{
        String originalFilename = file.getOriginalFilename();//获取文件名
        byte[] bytes = file.getBytes();//获取文件内容
        String filenameExtension =
            StringUtils.getFilenameExtension(file.getOriginalFilename());//获取文件后缀

        //封装
        FastDFSFile fastDFSFile = new FastDFSFile(originalFilename,bytes,filenameExtension);
        //文件上传
        String[] strings = FileUtil.fileUpload(fastDFSFile);
        return strings[0]+"/"+strings[1];
    }




}
