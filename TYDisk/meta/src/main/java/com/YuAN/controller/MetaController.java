package com.YuAN.controller;

import com.YuAN.Do.MetaFile;
import com.YuAN.dto.CompleteChunkDto;
import com.YuAN.dto.FileMeta;
import com.YuAN.response.CommonResponse;
import com.YuAN.services.MetaService;
import org.springframework.web.bind.annotation.*;

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
    public CommonResponse<?> generate(@RequestBody FileMeta fileMeta){
        MetaFile generate = metaService.generate(fileMeta);
        return CommonResponse.success(generate);
    }

    /**
     * 获取meta元数据
     *
     * @return meta元数据
     */
    @GetMapping("/info")
    public CommonResponse<?> info(){
        return null;
    }

    /**
     * 分片上传完成
     *
     * @return success
     */
    @PostMapping("/chunk/complete")
    public CommonResponse<?> chunkComplete(@RequestBody CompleteChunkDto completeChunk){
        metaService.completeChunk(completeChunk);
        return CommonResponse.success();
    }

    /**
     * 分片信息
     *
     * @return 文件分片信息
     */
    @GetMapping("/chunk/info")
    public CommonResponse<?> chunkInfo(){
        return null;
    }
}
