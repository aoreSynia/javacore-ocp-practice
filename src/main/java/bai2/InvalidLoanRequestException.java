package bai2;

public class InvalidLoanRequestException extends RuntimeException {
    public InvalidLoanRequestException(String message) {
        super(message);
    }
}
