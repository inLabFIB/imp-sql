package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLFloat implements PrimitiveConstant {

    private final float value;

    public SQLFloat(float value) {
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
        return o instanceof SQLFloat f
            && value == f.value;
    }
}
