package edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLPrimitiveString implements PrimitiveConstant {

    private final String value;

    public SQLPrimitiveString(String value) {
        this.value = Objects.requireNonNull(value, "The parameter 'value' cannot be null.");
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLPrimitiveString that = (SQLPrimitiveString) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String computeDefaultColumnAlias() {
        return value;
    }
}
