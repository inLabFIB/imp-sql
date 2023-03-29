package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class PredicateOperation implements BooleanExpression {



    public enum PredicateOperator {
        AND
    }
    private final PredicateOperator operator;

    private final BooleanExpression leftExpression;
    private final BooleanExpression rightExpression;
    public PredicateOperation(PredicateOperator operator, BooleanExpression leftExpression, BooleanExpression rightExpression) {
        this.operator = Objects.requireNonNull(operator, "The parameter 'operator' cannot be null.");
        this.leftExpression = Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null.");
        this.rightExpression = Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PredicateOperation that = (PredicateOperation) o;

        if (operator != that.operator) return false;
        if (!leftExpression.equals(that.leftExpression)) return false;
        return rightExpression.equals(that.rightExpression);
    }

    @Override
    public int hashCode() {
        int result = operator.hashCode();
        result = 31 * result + leftExpression.hashCode();
        result = 31 * result + rightExpression.hashCode();
        return result;
    }

}
