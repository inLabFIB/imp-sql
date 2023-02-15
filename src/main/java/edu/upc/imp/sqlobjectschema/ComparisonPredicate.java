package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class ComparisonPredicate extends Predicate {

    public enum ComparisonOperator {
        EQ
    }

    public ComparisonOperator operator;
    private ValueExpression leftExpression;
    private ValueExpression rightExpression;

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
