package com.YuAN.controller;

import com.YuAN.BO.FileChunkMeta;
import com.YuAN.BO.MetaFile;
import com.YuAN.Dto.FileMeta;
import com.YuAN.Vo.BucketVO;
import com.YuAN.Vo.MetaFileVo;
import com.YuAN.errors.BusinessException;
import com.YuAN.errors.EnumClientException;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import com.YuAN.utils.Md5Util;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
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
    public CommonResponse<String> upload(@RequestParam("bucketName") @NotBlank(message = "存储桶不得为空") String bucketName,
                                         @RequestPart @RequestParam("file") @NotNull(message = "文件不得为空") MultipartFile file){
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
    public CommonResponse<?> meta(@RequestBody @Valid FileMeta fileMeta){
        MetaFileVo meta = fileService.meta(fileMeta);
        return CommonResponse.success(meta);
    }

    /**
     * 分片文件上传
     *
     * @param bucketName 文件存储桶名称
     * @param filename 完整的文件访问路径
     * @param md5 分片的 md5
     * @param chunkNo 分片序号
     * @param file 分片文件
     * @return MD5
     */
    @PostMapping("/chunk/upload")
    public CommonResponse<?> chunkUpload(@RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                         @RequestParam("filename") @NotBlank(message = "文件名不得为空") String filename,
                                         @RequestParam("md5") @NotBlank(message = "文件 MD5 不得为空") String md5,
                                         @RequestParam("chunkNo") @Min(value = 0, message = "分片序号不得为空") Integer chunkNo,
                                         @RequestParam("file") @NotNull(message = "文件不得空") MultipartFile file) {
        String fileMd5 = fileService.uploadChunk(bucketName, filename, md5, chunkNo, file);
        return CommonResponse.success(fileMd5);
    }


    /**
     * 文件下载
     *
     * @param response response
     * @param bucketName 存储桶名
     * @param filename 文件名
     */
    @GetMapping("{bucketName}/{filename}")
    public void download(HttpServletResponse response,
                         @PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName,
                         @PathVariable @NotBlank(message = "文件名不得为空")  String filename){
        MetaFile metaFile = fileService.getMeta(bucketName,filename);
        response.reset();
        response.setContentType("application/octet-stream");
        response.setCharacterEncoding("utf-8");
        response.setContentLength(metaFile.getFilesize().intValue());
        response.setHeader("Content-Disposition","attachment;filename"+metaFile.getFilename());
        for (FileChunkMeta chunk : metaFile.getChunks()) {
            byte[] content = fileService.downloadChunk(chunk);
            String md5 = Md5Util.getMd5(content);
            if (!md5.equals(chunk.getChunkMd5())) {
                throw new
                        BusinessException(EnumClientException.THE_SHARD_FILE_IS_INCOMPLETE);
            }
            try {
                response.getOutputStream().write(content);
            } catch (Exception e) {
                throw new
                        BusinessException(EnumClientException.FAILED_TO_DOWNLOAD_FILE);
            }
        }
    }
    /**
     * 获取文件列表
     *
     * @return 文件列表
     */
    @GetMapping("/files")
    public CommonResponse<?> files() {
        List<BucketVO> obj = fileService.files();
        return CommonResponse.success(obj);
    }
    /**
     * 删除文件
     *
     * @param bucketName 文件存储桶
     * @param fileName 文件名
     * @return success
     */
    @DeleteMapping("/{bucketName}/{fileName}")
    public CommonResponse<Void> delete(@PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName,
                                       @PathVariable @NotBlank(message = "文件名不得为空") String fileName) {
            fileService.delete(bucketName, fileName);
            return CommonResponse.success();
}

}
