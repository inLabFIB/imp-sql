package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;

public class RepeatedTableAliasException extends IMPSqlException {

    public RepeatedTableAliasException() {
        super();
    }
    public RepeatedTableAliasException(String message) {
        super(message);
    }
}
