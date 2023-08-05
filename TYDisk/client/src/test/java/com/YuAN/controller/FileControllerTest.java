package com.YuAN.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.FileInputStream;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Test
    public void testUpload() throws Exception {
        File file = new File("E:\\Desktop\\test\\testword.doc");

        FileInputStream fileInputStream = new FileInputStream(file);
        MockMultipartFile multipartFile = new MockMultipartFile("file",
                fileInputStream);
        mockMvc.perform(
                        MockMvcRequestBuilders.multipart("/upload")
                                .file(multipartFile)
                                .param("bucketName", "test")
                )
                .andDo(print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}

