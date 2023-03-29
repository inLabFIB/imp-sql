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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Check c
            && getName().equals(c.getName())
            && expression.equals(c.expression);
    }
}
