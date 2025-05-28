package edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ValueExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class ComparisonPredicate extends Predicate {

    public enum ComparisonOperator {
        EQ,
        NEQ,
        LT,
        LEQ,
        GT,
        GEQ
    }
    private final ComparisonOperator operator;

    private final ValueExpression leftExpression;
    private final ValueExpression rightExpression;
    public ComparisonPredicate(ComparisonOperator operator, ValueExpression leftExpression, ValueExpression rightExpression) {
        this.operator = Objects.requireNonNull(operator, "The parameter 'operator' cannot be null.");
        this.leftExpression = Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null.");
        this.rightExpression = Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.");
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
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComparisonPredicate that = (ComparisonPredicate) o;

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
