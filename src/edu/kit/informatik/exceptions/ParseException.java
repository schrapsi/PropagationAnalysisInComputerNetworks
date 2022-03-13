package edu.kit.informatik.exceptions;

/**
 * This class is and exception for wrong string inputs.
 * @author ucfoh
 * @version 1.0
 */
public class ParseException extends Exception {

    private static final long serialVersionUID = -4688107508137905858L;

    /**
     * Constructor for returning a given Message
     * @param message with the Error
     */
    public ParseException(String message) {
        super(message);
    }
}
