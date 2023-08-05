package com.YuAN.Do;

import lombok.Data;
import lombok.experimental.Accessors;
import nonapi.io.github.classgraph.json.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document("meta")
@Accessors(chain = true)
public class MetaFile {
    @Id
    private String filename;
    private String extension;
    private Long filesize;
    private String bucketName;
    private Integer totalChunk;
    private List<FileChunkMeta> chunks;

}
