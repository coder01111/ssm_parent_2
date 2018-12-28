package com.pyg.shop_web;

import com.pinyougou.entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pyg.utils.FastDFSClient;

/**
 * 文件上传的类
 */
@RestController
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;//文件服务器地址

    @RequestMapping("/upload")
    public Result upload(MultipartFile file) {
        //1、取文件的扩展名,先获取文件的原始名字
        String filename = file.getOriginalFilename();
        //最后一个点后面的内容不包含点
        String extName = filename.substring(filename.lastIndexOf(".") + 1);
        try {
//2、创建一个 FastDFS 的客户端，加载配置文件
            FastDFSClient fastDFSClient
                    = new FastDFSClient("classpath:config/fdfs_client.conf");
            //3、执行上传处理
            String path = fastDFSClient.uploadFile(file.getBytes(),extName);
            //4、拼接返回的 url 和 ip 地址，拼装成完整的 url
            String url = FILE_SERVER_URL + path;
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }
    }
}

