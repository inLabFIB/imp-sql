package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions;

public class RepeatedAttributeNamesInSameTable extends RuntimeException {
    public RepeatedAttributeNamesInSameTable() {
        super("Repeated attribute names in table definition.");
    }
    public RepeatedAttributeNamesInSameTable(String message) {
        super(message);
    }
}
