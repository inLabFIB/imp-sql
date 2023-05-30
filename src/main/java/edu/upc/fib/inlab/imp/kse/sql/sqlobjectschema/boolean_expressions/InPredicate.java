package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;

import java.util.Objects;

public abstract class InPredicate extends Predicate {

    private final ValueExpression mainExpression;

    public InPredicate(ValueExpression mainExpression) {
        this.mainExpression = Objects.requireNonNull(mainExpression, "The parameter 'mainExpression' cannot be null.");
    }

    public ValueExpression getMainExpression() {
        return mainExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InPredicate that = (InPredicate) o;

        return mainExpression.equals(that.mainExpression);
    }

    @Override
    public int hashCode() {
        return mainExpression.hashCode();
    }
}
