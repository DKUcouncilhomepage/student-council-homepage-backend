package com.dku.council.mock;

import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class MultipartFileMock {
    public static MultipartFile create(String title, String ext){
        return new DummyMultipartFile("file", title + "." + ext);
    }
    public static List<MultipartFile> createList(int size) {
        return createList(size, "txt");
    }

    public static List<MultipartFile> createList(int size, String ext) {
        List<MultipartFile> files = new ArrayList<>(size);
        for (int i = 1; i <= size; i++) {
            files.add(new DummyMultipartFile("file", "myFile" + i + "." + ext));
        }
        return files;
    }
}
