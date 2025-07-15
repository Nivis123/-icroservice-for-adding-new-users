package ru.shift.userimporter.api.dto;

import ru.shift.userimporter.api.error.ErrorDTO;

import java.util.List;

public class DetailedFileStatisticDTO {
    private Long fileId;
    private String status;
    private StatisticDTO statistic;
    private List<ErrorDTO> errors;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public StatisticDTO getStatistic() { return statistic; }
    public void setStatistic(StatisticDTO statistic) { this.statistic = statistic; }
    public List<ErrorDTO> getErrors() { return errors; }
    public void setErrors(List<ErrorDTO> errors) { this.errors = errors; }
}