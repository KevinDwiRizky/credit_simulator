package exception;

public class LoanLoadException extends RuntimeException {
    public LoanLoadException(String message) {
        super(message);
    }
}
