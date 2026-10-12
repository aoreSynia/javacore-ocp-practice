package bai4;

/**
 * Ngoại lệ ném ra khi cố gắng đảo một bút toán đã từng được đảo trước đó.
 */
public class AlreadyReversedException extends RuntimeException {
    public AlreadyReversedException(String message) {
        super(message);
    }
}

