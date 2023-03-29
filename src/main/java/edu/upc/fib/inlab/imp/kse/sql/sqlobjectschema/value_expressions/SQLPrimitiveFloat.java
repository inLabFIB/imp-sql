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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLPrimitiveFloat f
            && value == f.value;
    }
}
