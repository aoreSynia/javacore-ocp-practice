package bai4;

/**
 * Ngoại lệ ném ra khi vượt hạn mức chuyển tiền trong ngày (500.000.000 đ/ngày).
 */
public class DailyLimitExceededException extends RuntimeException {
    public DailyLimitExceededException(String message) {
        super(message);
    }
}

