package com.YuAN.utils;

import com.YuAN.dto.FileMeta;

public interface FilenameGenerator {
    String generate(FileMeta fileMeta,Object... args);
}
