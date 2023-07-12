package edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableConstraint that = (TableConstraint) o;

        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
