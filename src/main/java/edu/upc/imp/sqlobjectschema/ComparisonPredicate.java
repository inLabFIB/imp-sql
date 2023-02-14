package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class ComparisonPredicate extends Predicate {

    enum ComparisonOperator {
        EQ
    }

    private ComparisonOperator operator;
    private ValueExpression leftExpression;
    private ValueExpression rightExpression;

    @Override
    public String visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
