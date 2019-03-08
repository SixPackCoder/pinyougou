package com.pinyougou.manager.controller;

import com.pinyougou.common.util.FastDFSClient;
import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        //获取文件名
        String originalFilename = file.getOriginalFilename();
        //获取后缀名
        String s = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        try {
            //创建一个 FastDFS 的客户端
            FastDFSClient client = new FastDFSClient("classpath:config/fdfs_client.conf"); //上传处理
            String path = client.uploadFile(file.getBytes(), s);
            //拼接返回的 url 和 ip 地址，拼装成完整的 url
            String url = FILE_SERVER_URL + path;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败!");
        }

    }
}
