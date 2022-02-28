package edu.kit.informatik.exceptions;

/**
 * This class is and exception for wrong string inputs.
 * @author ucfoh
 * @version 1.0
 */
public class ParseException extends Exception {

    /**
     * Constructor for returning a given Message
     * @param message with the Error
     */
    public ParseException(String message) {
        super(message);
    }
}
