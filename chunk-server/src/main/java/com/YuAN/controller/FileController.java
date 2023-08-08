package com.YuAN.controller;

import com.YuAN.entity.FileChunkDto;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Validated
@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/write")
    public CommonResponse<String> write(@Valid @RequestBody FileChunkDto fileChunkDto){
        String fileMd5 = fileService.write(fileChunkDto);
        return CommonResponse.success(fileMd5);
    }
    @GetMapping("/read")
    public CommonResponse<?> read(@RequestParam ("filename") @NotBlank(message = "文件名不为空") String filename,
                                  @RequestParam ("extension") String extension,
                                  @RequestParam ("chunkNo") @NotNull(message = "文件分片序号不为空") Integer chunkNo,
                                  @RequestParam ("bucketName") @NotBlank(message = "文件存储桶不为空") String bucketName){
        byte[] content = fileService.read(filename, extension, chunkNo, bucketName);
        return CommonResponse.success(content);
    }
}
