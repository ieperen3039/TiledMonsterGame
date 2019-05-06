package NG.Actions;

/**
 * @author Geert van Ieperen created on 30-4-2019.
 */
public class BrokenMovementException extends RuntimeException {
    public BrokenMovementException(String message) {
        super(message);
    }

    public BrokenMovementException(String message, Throwable cause) {
        super(message, cause);
    }
}
