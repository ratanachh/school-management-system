package com.visor.school.academic.util;

import com.visor.school.common.api.ApiResponse;

/**
 * Helper for building ApiResponse from academic-service (delegates to common ApiResponse).
 */
public final class ApiResponseHelper {

    private ApiResponseHelper() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.success(data);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.success(data, message);
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.error(message);
    }
}
