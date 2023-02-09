package edu.upc.imp.queryschema;

public class JoinOperation implements RelationalExpression {
    enum JoinOperator {
        CROSS
    }

    private JoinOperator operator;
    private RelationalExpression leftExpression;
    private RelationalExpression rightExpression;
}
