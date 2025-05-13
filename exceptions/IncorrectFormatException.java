package exceptions;

import java.io.IOException;

public class IncorrectFormatException extends Exception {
    public IncorrectFormatException(String message) {
        super(message);
    }
}
