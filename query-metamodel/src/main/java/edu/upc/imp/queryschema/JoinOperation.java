package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class JoinOperation implements RelationalExpression, QuerySchemaObject {

    enum JoinOperator {
        CROSS
    }

    private JoinOperator operator;
    private RelationalExpression leftExpression;
    private RelationalExpression rightExpression;
    private BooleanExpression onClause;

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
