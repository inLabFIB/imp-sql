package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class ComparisonPredicate extends Predicate {

    enum ComparisonOperator {
        EQ
    }

    private ComparisonOperator operator;
    private ValueExpression leftExpression;
    private ValueExpression rightExpression;

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
