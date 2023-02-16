package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class Assertion implements SQLObjectSchemaEntity {

    private final String name;
    private final BooleanExpression booleanExpression;

    public Assertion(String name, BooleanExpression booleanExpression) {
        this.name = name;
        this.booleanExpression = booleanExpression;
    }


    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    public String getName() {
        return name;
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }
}
