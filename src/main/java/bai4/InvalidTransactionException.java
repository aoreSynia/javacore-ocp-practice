package bai4;

/**
 * Ngoại lệ ném ra khi giao dịch hoặc bút toán không hợp lệ (ví dụ: Nợ khác Có, số tiền âm, tự chuyển cho chính mình).
 */
public class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }
}

