package se.onlyfin.onlyfinbackend.model;

/**
 * This exception is meant to be used when a user can't be found in the user database but is required for a method
 */
public class NoSuchUserException extends Exception {
    /**
     * Constructs an exception using the passed-in message
     *
     * @param msg the error message
     */
    public NoSuchUserException(String msg) {
        super(msg);
    }

}
