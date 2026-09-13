package topicmanagement.dto;

public record ApiResponse<T>(boolean success, String message, T data, Object errors) {
    public static <T> ApiResponse<T> ok(String m, T d) {
        return new ApiResponse<>(true, m, d, null);
    }

    public static <T> ApiResponse<T> fail(String m, Object e) {
        return new ApiResponse<>(false, m, null, e);
    }
}
