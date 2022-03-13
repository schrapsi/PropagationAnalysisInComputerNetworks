package edu.kit.informatik.exceptions;

/**
 * This class is and exception if the tree can´t be build properly
 * @author ucfoh
 * @version 1.0
 */

public class InvalidTreeException extends RuntimeException {

    private static final long serialVersionUID = 1681939588890840272L;

    /**
     * Constructor for returning a given Message
     * @param message with the Error
     */
    public InvalidTreeException(String message) {
        super(message);
    }
}
