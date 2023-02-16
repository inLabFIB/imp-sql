package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class OnJoin extends JoinOperation {

    public enum JoinOperator {
        INNER,
        NATURAL,
        LEFT,
        RIGHT,
        FULL
    }

    private final JoinOperator operator;
    private final BooleanExpression onClause;

    public OnJoin(JoinOperator operator, RelationalExpression leftExpression, RelationalExpression rightExpression, BooleanExpression onClause) {
        super(leftExpression, rightExpression);
        this.operator = operator;
        this.onClause = onClause;
    }

    public JoinOperator getOperator() {
        return operator;
    }

    public BooleanExpression getOnClause() {
        return onClause;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
