package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class SQLPrimitiveFloat implements PrimitiveConstant {

    private final float value;

    public SQLPrimitiveFloat(float value) {
        this.value = value;
    }

    public float getValue() {
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

        SQLPrimitiveFloat that = (SQLPrimitiveFloat) o;

        return Float.compare(that.value, value) == 0;
    }

    @Override
    public int hashCode() {
        return (value != 0.0f ? Float.floatToIntBits(value) : 0);
    }
}
