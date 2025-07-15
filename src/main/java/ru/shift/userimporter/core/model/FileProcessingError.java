package ru.shift.userimporter.core.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class FileProcessingError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "file_id", nullable = false)
    private UploadedFile file;
    @Column(nullable = false)
    private int rowNumber;
    @Column(nullable = false)
    private String errorMessage;
    @Column(nullable = false)
    private String rawData;
}