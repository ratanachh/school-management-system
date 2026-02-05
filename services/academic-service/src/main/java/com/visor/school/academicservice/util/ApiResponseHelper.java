package com.visor.school.academicservice.util;

import com.visor.school.common.api.ApiResponse;

/**
 * Helper class to work around Kotlin's default parameter issues when calling from Java
 */
public class ApiResponseHelper {
    
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.Companion.success(data, null);
    }
    
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.Companion.success(data, message);
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.Companion.error(message);
    }
}
