package com.YuAN.controller;

import com.YuAN.Do.FileChunkMeta;
import com.YuAN.Do.MetaFile;
import com.YuAN.Vo.BucketVO;
import com.YuAN.dto.CompleteChunkDto;
import com.YuAN.dto.FileMeta;
import com.YuAN.response.CommonResponse;
import com.YuAN.services.MetaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Validated
@RequestMapping("/meta")
@RestController
public class MetaController {
    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    /**
     * 生成meta元数据
     *
     * @return meta元数据
     */
    @PostMapping("/generate")
    public CommonResponse<MetaFile> generate(@Validated @RequestBody FileMeta fileMeta){
        MetaFile generate = metaService.generate(fileMeta);
        return CommonResponse.success(generate);
    }

    /**
     * 获取meta元数据
     *
     * @return meta元数据
     */
    @GetMapping("/info")
    public CommonResponse<MetaFile> info(@RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                  @RequestParam("filename") @NotBlank(message = "文件名不得为空")String filename){
        MetaFile metaFile = metaService.meta(bucketName,filename);

        return CommonResponse.success(metaFile);

    }

    /**
     * 分片上传完成
     *
     * @return success
     */
    @PostMapping("/chunk/complete")
    public CommonResponse<Void> chunkComplete(@Validated @RequestBody CompleteChunkDto completeChunk){
        metaService.completeChunk(completeChunk);
        return CommonResponse.success();
    }

    /**
     * 分片信息
     *
     * @return 文件分片信息
     */
    @GetMapping("/chunk/info")
    public CommonResponse<?> chunkInfo(@RequestParam("bucketName") @NotBlank(message = "存储桶名不得为空") String bucketName,
                                       @RequestParam("fileName") @NotBlank(message = "文件名不得为空") String fileName,
                                       @RequestParam("chunkNo") @NotNull(message = "分片序号不得为空") Integer chunkNo
    ) {
        List<FileChunkMeta> chunks = metaService.chunkInfo(bucketName, fileName, chunkNo);
        return CommonResponse.success(chunks);
    }

    @GetMapping("/files")
    public CommonResponse<List<BucketVO>> files() {
        List<BucketVO> files = metaService.files();
        return CommonResponse.success(files);
    }
    @DeleteMapping("/{bucketName}/{fileName}")
    public CommonResponse<Void> delete(@PathVariable @NotBlank(message = "存储桶名不得为空") String bucketName,
                                       @PathVariable @NotBlank(message = "文件名不得为空") String fileName) {
            metaService.delete(bucketName, fileName);
            return CommonResponse.success();
}


}
