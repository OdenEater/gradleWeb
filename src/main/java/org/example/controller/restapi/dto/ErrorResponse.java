package org.example.controller.restapi.dto;

public class ErrorResponse {
    private final String code; // "ERROR"
    private final String message;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}