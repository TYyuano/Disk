package com.YuAN.controller;

import com.YuAN.entity.FileChunkDto;
import com.YuAN.response.CommonResponse;
import com.YuAN.service.FileService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/write")
    public CommonResponse<Long> write(@Valid @RequestBody FileChunkDto fileChunkDto){
        long filesize = fileService.write(fileChunkDto);
        return CommonResponse.success(filesize);
    }
    @GetMapping("/read")
    public CommonResponse<?> read(@RequestParam ("filename") String filename,
                                  @RequestParam ("extension") String extension,
                                  @RequestParam ("chunkNo") Integer chunkNo,
                                  @RequestParam ("bucketName") String bucketName){
        byte[] content = fileService.read(filename, extension, chunkNo, bucketName);
        return CommonResponse.success(content);
    }
}
