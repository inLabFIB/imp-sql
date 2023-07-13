package edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions;

public class NonAliasedFromClauseSubQueryException extends RuntimeException {

    public NonAliasedFromClauseSubQueryException() {
        super();
    }
    public NonAliasedFromClauseSubQueryException(String message) {
        super(message);
    }
}
