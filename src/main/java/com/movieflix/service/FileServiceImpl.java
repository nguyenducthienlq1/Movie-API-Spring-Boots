package com.movieflix.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileServiceImpl implements FileService{

    @Override
    public String uploadFile(String path, MultipartFile file) throws IOException {

        //Lấy tên file
        String fileName = file.getOriginalFilename();
        //Lấy đường dẫn
        String filePath = path + File.separator + fileName;

        //Tạo file
        File f = new File(path);
        if(!f.exists()) {
            f.mkdirs();
        }
        //Copy File và upload File tới đường dẫn
        Files.copy(file.getInputStream(), Paths.get(filePath));
        return fileName;
    }

    @Override
    public InputStream getResourceFile(String path, String fileName) throws FileNotFoundException {

        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }
}
