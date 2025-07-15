package ru.shift.userimporter.core.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "uploaded_files")
@Data
public class UploadedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String originalFilename;
    @Column(nullable = false)
    private String storagePath;
    @Column(nullable = false, length = 20)
    private String status; // NEW, IN_PROGRESS, DONE, FAILED
    @Column(nullable = false)
    private int totalRows;
    @Column(nullable = false)
    private int processedRows;
    @Column(nullable = false)
    private int validRows;
    @Column(nullable = false)
    private int invalidRows;
    @Column(length = 64)
    private String hash;

}