package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class ComparisonPredicate extends Predicate {

    public enum ComparisonOperator {
        EQ
    }

    private final ComparisonOperator operator;
    private final ValueExpression leftExpression;
    private final ValueExpression rightExpression;

    public ComparisonPredicate(ComparisonOperator operator, ValueExpression leftExpression, ValueExpression rightExpression) {
        this.operator = operator;
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public ValueExpression getLeftExpression() {
        return leftExpression;
    }

    public ValueExpression getRightExpression() {
        return rightExpression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof ComparisonPredicate cp
            && operator.equals(cp.operator)
            && leftExpression.equals(cp.leftExpression)
            && rightExpression.equals(cp.rightExpression);
    }
}
