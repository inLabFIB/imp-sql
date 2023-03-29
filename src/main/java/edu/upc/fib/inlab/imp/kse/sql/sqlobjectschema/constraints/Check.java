package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Check check = (Check) o;

        if (getName() != null ? !getName().equals(check.getName()) : check.getName() != null) return false;
        return expression.equals(check.expression);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + expression.hashCode();
        return result;
    }

}
