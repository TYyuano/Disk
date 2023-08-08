package com.YuAN.controller;

import com.YuAN.entity.FileChunkDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@AutoConfigureMockMvc
@SpringBootTest
public class FileControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;
    @Test
    public void testWrite() throws Exception {
        FileChunkDto fileChunkDto = new FileChunkDto();
        fileChunkDto.setBucketName("test");
        fileChunkDto.setFilename("testfile2023080420020928@yuan");
        fileChunkDto.setChunkNo(0);
        fileChunkDto.setExtension(".pdf");
        fileChunkDto.setChunkSize(50);
        fileChunkDto.setBytes("asdfghjklqwretyuiop".getBytes());
        String s = mapper.writeValueAsString(fileChunkDto);
        mvc.perform(MockMvcRequestBuilders.post("/file/write")
                .content(s)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());

    }
    @Test
    public void testRead() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/file/read")
                        .param("filename",
                                "testfile2023080420020928@yuan")
                        .param("extension", ".jpg")
                        .param("chunkNo", "0")
                        .param("bucketName", "test"))
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

}
