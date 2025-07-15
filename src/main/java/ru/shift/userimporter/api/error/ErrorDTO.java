package ru.shift.userimporter.api.error;

public class ErrorDTO {
    private int rowNumber;
    private String errorMessage;
    private String rawData;

    public ErrorDTO(int rowNumber, String errorMessage, String rawData) {
        this.rowNumber = rowNumber;
        this.errorMessage = errorMessage;
        this.rawData = rawData;
    }

    public int getRowNumber() { return rowNumber; }
    public void setRowNumber(int rowNumber) { this.rowNumber = rowNumber; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public String getRawData() { return rawData; }
    public void setRawData(String rawData) { this.rawData = rawData; }
}