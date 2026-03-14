package com.dattakrupa.laundry.dto;

public class ApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;

    ApiResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Success response
    public static <T> ApiResponseDTO<T> success(String message, T data) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    // Error response
    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .build();
    }

    public static <T> ApiResponseDTOBuilder<T> builder() {
        return new ApiResponseDTOBuilder<T>();
    }

    public boolean isSuccess() {
        return this.success;
    }

    public String getMessage() {
        return this.message;
    }

    public T getData() {
        return this.data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof ApiResponseDTO)) return false;
        final ApiResponseDTO<?> other = (ApiResponseDTO<?>) o;
        if (!other.canEqual((Object) this)) return false;
        if (this.isSuccess() != other.isSuccess()) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$data = this.getData();
        final Object other$data = other.getData();
        if (this$data == null ? other$data != null : !this$data.equals(other$data)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ApiResponseDTO;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        result = result * PRIME + (this.isSuccess() ? 79 : 97);
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $data = this.getData();
        result = result * PRIME + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "ApiResponseDTO(success=" + this.isSuccess() + ", message=" + this.getMessage() + ", data=" + this.getData() + ")";
    }

    public static class ApiResponseDTOBuilder<T> {
        private boolean success;
        private String message;
        private T data;

        ApiResponseDTOBuilder() {
        }

        public ApiResponseDTOBuilder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public ApiResponseDTOBuilder<T> message(String message) {
            this.message = message;
            return this;
        }

        public ApiResponseDTOBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponseDTO<T> build() {
            return new ApiResponseDTO<T>(this.success, this.message, this.data);
        }

        public String toString() {
            return "ApiResponseDTO.ApiResponseDTOBuilder(success=" + this.success + ", message=" + this.message + ", data=" + this.data + ")";
        }
    }
}