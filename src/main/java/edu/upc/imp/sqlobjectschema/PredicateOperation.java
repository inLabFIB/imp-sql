package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class PredicateOperation implements BooleanExpression {

    public enum PredicateOperator {
        AND
    }

    private final PredicateOperator operator;
    private final BooleanExpression leftExpression;
    private final BooleanExpression rightExpression;

    public PredicateOperation(PredicateOperator operator, BooleanExpression leftExpression, BooleanExpression rightExpression) {
        this.operator = operator;
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public PredicateOperator getOperator() {
        return operator;
    }

    public BooleanExpression getLeftExpression() {
        return leftExpression;
    }

    public BooleanExpression getRightExpression() {
        return rightExpression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof PredicateOperation pop
            && operator.equals(pop.operator)
            && leftExpression.equals(pop.leftExpression)
            && rightExpression.equals(pop.rightExpression);
    }
}
