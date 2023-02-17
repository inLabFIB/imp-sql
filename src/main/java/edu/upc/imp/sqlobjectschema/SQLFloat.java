package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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
}
