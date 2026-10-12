package bai4;

/**
 * Ngoại lệ ném ra khi gửi lại cùng idempotencyKey nhưng nội dung yêu cầu khác nhau.
 */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}

