package com.dedupman.dedupman.model;

public class FileResponse {

    private String fileName;
    private String status; // "Unique", "Duplicate", "Error"

    public FileResponse() {
        // Default constructor needed for JSON serialization/deserialization
    }

    public FileResponse(String fileName, String status) {
        this.fileName = fileName;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) { // Added setter
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) { // Added setter
        this.status = status;
    }
}
