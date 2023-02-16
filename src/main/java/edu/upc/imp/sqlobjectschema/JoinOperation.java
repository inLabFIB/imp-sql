package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class JoinOperation implements RelationalExpression {

    enum JoinOperator {
        CROSS
    }

    private JoinOperator operator;
    private RelationalExpression leftExpression;
    private RelationalExpression rightExpression;
    private BooleanExpression onClause;

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
