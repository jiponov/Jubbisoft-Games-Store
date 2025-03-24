package app.shared.exception;

public class GameAlreadyExistException extends RuntimeException {

    public GameAlreadyExistException() {

    }

    public GameAlreadyExistException(String message) {
    super(message);
    }
}