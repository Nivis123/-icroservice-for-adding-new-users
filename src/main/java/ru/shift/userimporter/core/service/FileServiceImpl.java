package ru.shift.userimporter.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.shift.userimporter.core.model.UploadedFile;
import ru.shift.userimporter.core.model.User;
import ru.shift.userimporter.core.model.FileProcessingError;
import ru.shift.userimporter.core.repository.UploadedFileRepository;
import ru.shift.userimporter.core.repository.UserRepository;
import ru.shift.userimporter.core.repository.FileProcessingErrorRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

@Service
public class FileServiceImpl implements FileService {
    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private final UploadedFileRepository uploadedFileRepository;
    private final UserRepository userRepository;
    private final FileProcessingErrorRepository errorRepository;

    public FileServiceImpl(UploadedFileRepository uploadedFileRepository,
                           UserRepository userRepository,
                           FileProcessingErrorRepository errorRepository) {
        this.uploadedFileRepository = uploadedFileRepository;
        this.userRepository = userRepository;
        this.errorRepository = errorRepository;
    }

    @Override
    public String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String storagePath = uploadDir + "/" + System.currentTimeMillis() + "_" + originalFilename.replace(" ", "_");
        Files.createDirectories(Paths.get(uploadDir));
        Files.write(Paths.get(storagePath), file.getBytes());

        UploadedFile uploadedFile = new UploadedFile();
        uploadedFile.setOriginalFilename(originalFilename);
        uploadedFile.setStoragePath(storagePath);
        uploadedFile.setStatus("NEW");
        uploadedFile.setTotalRows(0);
        uploadedFile.setProcessedRows(0);
        uploadedFile.setValidRows(0);
        uploadedFile.setInvalidRows(0);
        uploadedFile.setHash(generateHash(storagePath));

        UploadedFile savedFile = uploadedFileRepository.save(uploadedFile);
        return "File uploaded successfully, ID: " + savedFile.getId();
    }


    @Override
    @Async
    public void processFile(Long fileId) {
        UploadedFile file = uploadedFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
        file.setStatus("IN_PROGRESS");
        uploadedFileRepository.save(file);

        try (BufferedReader br = new BufferedReader(new FileReader(file.getStoragePath()))) {
            String line;
            int totalRows = 0, validRows = 0, invalidRows = 0;
            while ((line = br.readLine()) != null) {
                totalRows++;
                try {
                    String[] data = line.split(",");
                    if (data.length < 5 || data.length > 6) throw new IllegalArgumentException("Invalid number of fields. Expected 5 or 6.");

                    User user = new User();
                    validateAndSetName(user, "firstName", data[0], true);
                    validateAndSetName(user, "lastName", data[1], true);
                    validateAndSetName(user, "middleName", data.length > 2 ? data[2] : "", false);
                    validateEmail(user, data[3]);
                    validatePhone(user, data[4]);
                    validateBirthDate(user, data.length > 5 ? data[5] : null);

                    LocalDateTime now = LocalDateTime.now();
                    user.setCreatedAt(now);
                    user.setUpdatedAt(now);
                    userRepository.save(user);
                    validRows++;
                } catch (Exception e) {
                    FileProcessingError error = new FileProcessingError();
                    error.setFile(file);
                    error.setRowNumber(totalRows);
                    error.setErrorMessage(e.getMessage());
                    error.setRawData(line);
                    errorRepository.save(error);
                    invalidRows++;
                }
            }
            file.setTotalRows(totalRows);
            file.setProcessedRows(totalRows);
            file.setValidRows(validRows);
            file.setInvalidRows(invalidRows);
            file.setStatus(invalidRows > 0 ? "DONE" : "DONE");
            uploadedFileRepository.save(file);
            System.out.println("Processing completed for fileId: " + fileId + ", status: " + file.getStatus());
        } catch (IOException e) {
            file.setStatus("FAILED");
            uploadedFileRepository.save(file);
        }
    }

    private void validateAndSetName(User user, String fieldName, String value, boolean required) throws IllegalArgumentException {
        if (required && (value == null || value.trim().isEmpty())) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        if (!required && (value == null || value.trim().isEmpty())) {
            return;
        }
        if (value.length() < 3 || value.length() > 50) {
            throw new IllegalArgumentException(fieldName + " length must be between 3 and 50");
        }
        if (!value.matches("^[А-Я][А-Яа-я\\-'\\s]*$")) {
            throw new IllegalArgumentException(fieldName + " must start with uppercase Cyrillic and contain only Cyrillic, -, ' or space");
        }
        if (fieldName.equals("firstName")) user.setFirstName(value);
        else if (fieldName.equals("lastName")) user.setLastName(value);
        else if (fieldName.equals("middleName")) user.setMiddleName(value);
    }

    private void validateEmail(User user, String email) throws IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (email.length() > 100) {
            throw new IllegalArgumentException("Email length must not exceed 100 characters");
        }
        String emailPattern = "^[A-Za-z0-9+_.-]+@shift\\.(com|ru)$";
        if (!Pattern.matches(emailPattern, email)) {
            throw new IllegalArgumentException("Email must match shift.com or shift.ru domain and standard email format");
        }
        user.setEmail(email);
    }

    private void validatePhone(User user, String phone) throws IllegalArgumentException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required");
        }
        if (phone.length() != 11 || !phone.startsWith("7") || !phone.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Phone must be 11 digits starting with 7");
        }
        user.setPhone(phone);
    }

    private void validateBirthDate(User user, String birthDateStr) throws IllegalArgumentException {
        if (birthDateStr == null || birthDateStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Birth date is required");
        }
        LocalDate birthDate = LocalDate.parse(birthDateStr);
        LocalDate minDate = LocalDate.now().minusYears(18);
        if (birthDate.isAfter(LocalDate.now()) || birthDate.isAfter(minDate)) {
            throw new IllegalArgumentException("Birth date must be within 18 years from now and not in the future");
        }
        user.setBirthDate(birthDate);
    }

    private String generateHash(String storagePath) throws IOException {
        return generateHashFromInputStream(new FileInputStream(storagePath));
    }

    @Override
    public String generateHashFromInputStream(InputStream inputStream) throws IOException { 
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        byte[] hash = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}