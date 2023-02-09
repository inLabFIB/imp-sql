package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class PredicateOperation implements BooleanExpression, QuerySchemaObject {

    enum PredicateOperator {
        AND
    }

    private PredicateOperator operator;
    private BooleanExpression leftExpression;
    private BooleanExpression rightExpression;

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
