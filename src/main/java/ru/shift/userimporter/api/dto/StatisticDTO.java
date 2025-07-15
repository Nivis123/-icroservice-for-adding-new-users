package ru.shift.userimporter.api.dto;

public class StatisticDTO {
    private int totalRows;
    private int processedRows;
    private int validRows;
    private int invalidRows;

    public StatisticDTO(int totalRows, int processedRows, int validRows, int invalidRows) {
        this.totalRows = totalRows;
        this.processedRows = processedRows;
        this.validRows = validRows;
        this.invalidRows = invalidRows;
    }

    public int getTotalRows() { return totalRows; }
    public void setTotalRows(int totalRows) { this.totalRows = totalRows; }
    public int getProcessedRows() { return processedRows; }
    public void setProcessedRows(int processedRows) { this.processedRows = processedRows; }
    public int getValidRows() { return validRows; }
    public void setValidRows(int validRows) { this.validRows = validRows; }
    public int getInvalidRows() { return invalidRows; }
    public void setInvalidRows(int invalidRows) { this.invalidRows = invalidRows; }
}