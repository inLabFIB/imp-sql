package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;

public class NonAliasedFromClauseSubQueryException extends IMPSqlException {

    public NonAliasedFromClauseSubQueryException() {
        super();
    }
    public NonAliasedFromClauseSubQueryException(String message) {
        super(message);
    }
}
