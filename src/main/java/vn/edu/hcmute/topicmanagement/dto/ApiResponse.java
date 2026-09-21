package vn.edu.hcmute.topicmanagement.dto;

public record ApiResponse<T>(boolean success, String message, T data, Object errors) {
    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null);
    }

    public static <T> ApiResponse<T> ok(String message) {
        return new ApiResponse<>(true, message, null, null);
    }

    public static <T> ApiResponse<T> fail(String message, Object errors) {
        return new ApiResponse<>(false, message, null, errors);
    }
}
