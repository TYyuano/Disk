package com.YuAN.utils.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.YuAN.dto.FileMeta;
import com.YuAN.utils.FilenameGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DefaultFilenameGenerate implements FilenameGenerator {
    @Override
    public String generate(FileMeta fileMeta, Object... args) {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter yyyyMMddHH = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String time = now.format(yyyyMMddHH);
//        return "f%s%s".formatted(time,md5(fileMeta,(String) args[0]));
        return "f%s%s".formatted(time,md5(fileMeta,args[0].toString()));
    }
    private String md5(FileMeta fileMeta,String address){
        return DigestUtil.md5Hex(
                "%s_%s_%s_%s".formatted(
                        address,
                        fileMeta.getFilesize(),
                        fileMeta.getBucketName(),
                        fileMeta.getExtension()
                )
        );
    }
}
