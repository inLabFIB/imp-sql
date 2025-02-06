package edu.upc.fib.inlab.imp.kse.sql.core.exceptions;

public class IMPSqlException extends RuntimeException {

    /**
     * Constructs a new IMP SQL exception with {@code null} as its detail message.
     */
    public IMPSqlException() {
        super();
    }

    /**
     * Constructs a new IMP SQL exception with the specified detail message.
     *
     * @param message the detail message. The detail message is saved for later retrieval by the {@link #getMessage()}
     *                method.
     */
    public IMPSqlException(String message) {
        super(message);
    }

}
