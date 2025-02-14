package edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * This DataType stores can store a Date + Time like YYYY:MM:DD - HH:MM:SS:...
 * fractionalSecondsPrecision is for when time is stored.
 */
public class SQLDateTime implements SQLDataType {
    private final Integer fractionalSecondsPrecision;

    public SQLDateTime() {
        this(0);
    }

    public SQLDateTime(Integer precision) {
        this.fractionalSecondsPrecision = Objects.requireNonNull(precision, "DateTime requires a fractional seconds precision value.");
    }

    public Integer getFractionalSecondsPrecision() {
        return fractionalSecondsPrecision;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLDateTime that = (SQLDateTime) o;

        return fractionalSecondsPrecision.equals(that.fractionalSecondsPrecision);
    }

    @Override
    public int hashCode() {
        return fractionalSecondsPrecision.hashCode();
    }
}
