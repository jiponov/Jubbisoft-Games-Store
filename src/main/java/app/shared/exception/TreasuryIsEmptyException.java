package app.shared.exception;

public class TreasuryIsEmptyException extends RuntimeException {

    public TreasuryIsEmptyException() {

    }


    public TreasuryIsEmptyException(String message) {
        super(message);
    }
}