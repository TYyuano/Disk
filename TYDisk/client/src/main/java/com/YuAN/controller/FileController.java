package com.YuAN.controller;

import cn.hutool.http.server.HttpServerResponse;
import com.YuAN.Dto.FileMeta;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /**
     * 小文件上传
     *
     * @param bucketName 存储桶名称
     * @param file 文件
     * @return 文件的访问路径
     */
    @PostMapping("/upload")
    public CommonResponse<String> upload(@RequestParam("bucketName") String bucketName,
                                    @RequestParam("file") MultipartFile file){
        String fileUrl = fileService.upload(bucketName,file);
        return CommonResponse.success(fileUrl);
    }

    /**
     * 生成meta信息
     *
     * @param fileMeta fileMeta
     * @return metaFile
     */
    @PostMapping("/meta")
    public CommonResponse<?> meta(@RequestBody FileMeta fileMeta){

        return null;
    }

    /**
     * 分片文件上传
     *
     * @param dileUrl 完整文件访问路径
     * @param md5 分片MD5
     * @param chunkNo 分片序号
     * @param file 分片文件
     * @return MD5
     */
    @PostMapping("/chunk/upload")
    public CommonResponse<?> chunkUpload(@RequestParam("fileUrl") String dileUrl,
                                         @RequestParam("md5") String md5,
                                         @RequestParam("chunkNo") Integer chunkNo,
                                         @RequestParam("file") MultipartFile file){
        return null;
    }

    /**
     * 文件下载
     *
     * @param response response
     * @param bucketName 存储桶名
     * @param fileName 文件名
     */
    @GetMapping("{bucketName}/{fileName}")
    public void download(HttpServerResponse response,
                         @PathVariable String bucketName,
                         @PathVariable String fileName){

    }
}
