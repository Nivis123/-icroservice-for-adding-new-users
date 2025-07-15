package ru.shift.userimporter.core.service;

import java.io.IOException;
import java.io.InputStream;

public interface FileService {
    String saveFile(org.springframework.web.multipart.MultipartFile file) throws IOException;
    void processFile(Long fileId);
    String generateHashFromInputStream(InputStream inputStream) throws IOException;
}