package com.konomusic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ApiResponse - Response chung cho tất cả API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Integer statusCode;

    public ApiResponse(boolean success, T data) {
        this.success = success;
        this.data = data;
        this.statusCode = success ? 200 : 400;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.statusCode = success ? 200 : 400;
    }

}

