package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints;

public abstract class TableConstraint implements Constraint {

    private final String name;

    protected TableConstraint(String name) {
        this.name = name;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        return name;
    }

}
