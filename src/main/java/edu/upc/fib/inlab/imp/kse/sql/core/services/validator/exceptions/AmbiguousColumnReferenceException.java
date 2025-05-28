package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;

public class AmbiguousColumnReferenceException extends IMPSqlException {

    public AmbiguousColumnReferenceException() {
        super();
    }
    public AmbiguousColumnReferenceException(String message) {
        super(message);
    }
}
