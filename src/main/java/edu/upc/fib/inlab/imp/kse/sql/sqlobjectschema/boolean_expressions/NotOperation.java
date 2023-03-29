package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof NotOperation nop
            && expression.equals(nop.expression);
    }
}
