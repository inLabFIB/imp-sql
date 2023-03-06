package edu.upc.imp.sqlobjectschema.value_expressions;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLPrimitiveInteger i
            && value == i.value;
    }
}
