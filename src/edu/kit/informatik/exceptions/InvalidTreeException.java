package edu.kit.informatik.exceptions;
/**
 * This class is and exception if the tree can´t be build properly
 * @author ucfoh
 * @version 1.0
 */
public class InvalidTreeException extends RuntimeException {

    public InvalidTreeException(String message) {
        super(message);
    }
}
