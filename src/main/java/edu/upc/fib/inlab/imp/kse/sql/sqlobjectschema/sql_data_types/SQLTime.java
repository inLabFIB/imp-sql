package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * This DataType stores can store a Date + Time like YYYY:MM:DD - HH:MM:SS:...
 * fractionalSecondsPrecision is for when time is stored.
 */
public class SQLTime implements SQLDataType {
    private final Integer fractionalSecondsPrecision;

    public SQLTime() {
        this(0);
    }

    public SQLTime(Integer precision) {
        this.fractionalSecondsPrecision = Objects.requireNonNull(precision, "Time requires a fractional seconds precision value.");
    }

    public Integer getFractionalSecondsPrecision() {
        return fractionalSecondsPrecision;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLTime sqlTime = (SQLTime) o;

        return fractionalSecondsPrecision.equals(sqlTime.fractionalSecondsPrecision);
    }

    @Override
    public int hashCode() {
        return fractionalSecondsPrecision.hashCode();
    }
}
