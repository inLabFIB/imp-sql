package edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Check extends TableConstraint {

    private final BooleanExpression expression;

    public Check(String name, BooleanExpression expression) {
        super(name);
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
        if (!super.equals(o)) return false;

        Check check = (Check) o;

        return Objects.equals(expression, check.expression);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        return result;
    }
}
