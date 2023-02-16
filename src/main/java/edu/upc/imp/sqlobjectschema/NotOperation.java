package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class NotOperation implements BooleanExpression {

    private final BooleanExpression expression;

    public NotOperation(BooleanExpression expression) {
        this.expression = expression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    public BooleanExpression getExpression() {
        return expression;
    }
}
