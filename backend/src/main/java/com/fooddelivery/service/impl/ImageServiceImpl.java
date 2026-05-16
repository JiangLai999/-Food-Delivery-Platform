package com.fooddelivery.service.impl;

import com.fooddelivery.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {
    private static final String BASE_DIR = System.getProperty("user.dir") + File.separator + "storage" + File.separator + "images";

    @Override
    public String saveImage(MultipartFile file, String subDir) throws IOException {
        if (file == null || file.isEmpty()) throw new IOException("Empty image file");
        String dirPath = BASE_DIR + File.separator + subDir;
        File dir = new File(dirPath);
        if (!dir.exists()) dir.mkdirs();
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) ext = original.substring(original.lastIndexOf('.'));
        String filename = System.currentTimeMillis() + ext;
        File out = new File(dir, filename);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(file.getBytes());
        }
        return "/images/" + subDir + "/" + filename;
    }
}
