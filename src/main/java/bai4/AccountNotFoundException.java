package bai4;

/**
 * Ngoại lệ ném ra khi tài khoản không tồn tại trong sổ cái.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

