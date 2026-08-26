package za.ac.cput.exception;

/** Thrown when a request is well-formed but violates a business rule (double-booking, expired promo, etc.). */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }
}
