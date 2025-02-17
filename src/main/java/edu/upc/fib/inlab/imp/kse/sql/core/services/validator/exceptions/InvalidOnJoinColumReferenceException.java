package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;

public class InvalidOnJoinColumReferenceException extends IMPSqlException {

    public InvalidOnJoinColumReferenceException() {
        super();
    }
    public InvalidOnJoinColumReferenceException(String message) {
        super(message);
    }
}
