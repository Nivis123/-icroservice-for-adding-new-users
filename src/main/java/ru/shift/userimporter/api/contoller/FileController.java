package ru.shift.userimporter.api.contoller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.shift.userimporter.api.dto.*;
import ru.shift.userimporter.core.model.FileProcessingError;
import ru.shift.userimporter.core.model.UploadedFile;
import ru.shift.userimporter.core.model.User;
import ru.shift.userimporter.core.service.FileService;
import ru.shift.userimporter.core.repository.UploadedFileRepository;
import ru.shift.userimporter.core.repository.FileProcessingErrorRepository;
import ru.shift.userimporter.core.repository.UserRepository;
import ru.shift.userimporter.api.error.ErrorDTO;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class FileController {

    private final FileService fileService;
    private final UploadedFileRepository uploadedFileRepository;
    private final FileProcessingErrorRepository errorRepository;
    private final UserRepository userRepository;

    @Autowired
    public FileController(FileService fileService, UploadedFileRepository uploadedFileRepository,
                          FileProcessingErrorRepository errorRepository, UserRepository userRepository) {
        this.fileService = fileService;
        this.uploadedFileRepository = uploadedFileRepository;
        this.errorRepository = errorRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/files")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        UploadedFile existingFile = uploadedFileRepository.findByHash(fileService.generateHashFromInputStream(file.getInputStream()))
                .orElse(null); // Извлекаем UploadedFile из Optional или null, если не найдено
        if (existingFile != null) {
            return new ResponseEntity<>("File with this hash already exists", HttpStatus.CONFLICT);
        }
        String fileId = fileService.saveFile(file);
        return new ResponseEntity<>(fileId, HttpStatus.CREATED);
    }

    @PostMapping("/files/{id}/process")
    public ResponseEntity<String> processFile(@PathVariable Long id) {
        try {
            fileService.processFile(id);
            return new ResponseEntity<>("Processing started for file ID: " + id, HttpStatus.ACCEPTED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>("Error: " + e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/files/statistics")
    public ResponseEntity<List<FileStatisticDTO>> getFileStatistics(@RequestParam(required = false) String status) {
        List<FileStatisticDTO> dtos = uploadedFileRepository.findAll().stream().map(file -> {
            FileStatisticDTO dto = new FileStatisticDTO();
            dto.setFileId(file.getId());
            dto.setStatus(file.getStatus());
            dto.setStatistic(new StatisticDTO(file.getTotalRows(), file.getProcessedRows(), file.getValidRows(), file.getInvalidRows()));
            dto.setHashCode(file.getHash().hashCode()); // Пример, можно улучшить
            return dto;
        }).filter(dto -> status == null || dto.getStatus().equals(status)).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/files/{id}/statistics")
    public ResponseEntity<DetailedFileStatisticDTO> getDetailedFileStatistic(@PathVariable Long id) {
        UploadedFile file = uploadedFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
        List<FileProcessingError> errors = errorRepository.findByFileId(id);
        DetailedFileStatisticDTO dto = new DetailedFileStatisticDTO();
        dto.setFileId(file.getId());
        dto.setStatus(file.getStatus());
        dto.setStatistic(new StatisticDTO(file.getTotalRows(), file.getProcessedRows(), file.getValidRows(), file.getInvalidRows()));
        dto.setErrors(errors.stream().map(error -> new ErrorDTO(error.getRowNumber(), error.getErrorMessage(), error.getRawData())).collect(Collectors.toList()));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    @GetMapping("/clients")
    public ResponseEntity<List<UserDTO>> getClients(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {
        List<User> users = userRepository.findAll(); // !!МОЖНО ЗАМЕНИТЬ НА ФУНКЦИЮ ПОИСКА ПО ПАРАМЕТРАМ
        List<UserDTO> dtos = users.stream()
                .filter(u -> phone == null || u.getPhone().equals(phone))
                .filter(u -> name == null || u.getFirstName().equals(name))
                .filter(u -> lastName == null || u.getLastName().equals(lastName))
                .filter(u -> email == null || u.getEmail().equals(email))
                .skip(offset)
                .limit(limit)
                .map(u -> new UserDTO(
                        u.getPhone(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getMiddleName(),
                        u.getEmail(),
                        u.getBirthDate(),
                        u.getCreatedAt(),
                        u.getUpdatedAt()
                )).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }

    @GetMapping("/files/{id}/errors")
    public ResponseEntity<List<FileProcessingError>> getFileErrors(@PathVariable Long id) {
        List<FileProcessingError> errors = errorRepository.findByFileId(id);
        return new ResponseEntity<>(errors, HttpStatus.OK);
    }
}