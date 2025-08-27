package org.example.controller.restapi.dto;

public class UploadResponse {
    private final String code; // "OK"
    private final long imageId;
    private final String fileName;

    public UploadResponse(String code, long imageId, String fileName) {
        this.code = code;
        this.imageId = imageId;
        this.fileName = fileName;
    }

    public String getCode() {
        return code;
    }

    public long getImageId() {
        return imageId;
    }

    public String getFileName() {
        return fileName;
    }
}