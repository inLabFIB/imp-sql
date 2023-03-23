package edu.upc.imp.sql.sqlobjectschema.relational_expressions;

public abstract class JoinOperation implements RelationalExpression {

    private final RelationalExpression leftExpression;
    private final RelationalExpression rightExpression;


    protected JoinOperation(RelationalExpression leftExpression, RelationalExpression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public RelationalExpression getLeftExpression() {
        return leftExpression;
    }

    public RelationalExpression getRightExpression() {
        return rightExpression;
    }
}
