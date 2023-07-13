package edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

public class SQLPrimitiveInteger implements PrimitiveConstant {

    private final int value;

    public SQLPrimitiveInteger(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLPrimitiveInteger that = (SQLPrimitiveInteger) o;

        return value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public String computeDefaultColumnAlias() {
        return null;
    }
}
