package edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class NotOperation implements BooleanExpression {

    private final BooleanExpression expression;

    public NotOperation(BooleanExpression expression) {
        this.expression = Objects.requireNonNull(expression, "The parameter 'expression' cannot be null.");
    }

    public BooleanExpression getExpression() {
        return expression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NotOperation that = (NotOperation) o;

        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        return expression.hashCode();
    }
}
