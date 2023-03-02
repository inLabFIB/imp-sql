package edu.upc.imp.sqlobjectschema;

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
