package ru.shift.userimporter.api.dto;

public class FileStatisticDTO {
    private Long fileId;
    private String status;
    private StatisticDTO statistic;
    private int hashCode;

    public Long getFileId() { return fileId; }
    public void setFileId(Long fileId) { this.fileId = fileId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public StatisticDTO getStatistic() { return statistic; }
    public void setStatistic(StatisticDTO statistic) { this.statistic = statistic; }
    public int getHashCode() { return hashCode; }
    public void setHashCode(int hashCode) { this.hashCode = hashCode; }
}