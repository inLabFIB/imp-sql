package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;

public class InvalidColumnReferenceException extends IMPSqlException {

    public InvalidColumnReferenceException(String columnReferenceName) {
        super("The columnReference (" + columnReferenceName + ") is not a valid reference.");
    }
}
