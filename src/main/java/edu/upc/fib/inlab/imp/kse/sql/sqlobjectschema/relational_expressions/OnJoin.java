package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class OnJoin extends JoinOperation {






    public enum JoinOperator {
        INNER,
        NATURAL,
        LEFT,
        RIGHT,
        FULL
    }
    private final JoinOperator operator;

    private final BooleanExpression onClause;
    public OnJoin(JoinOperator operator, RelationalExpression leftExpression, RelationalExpression rightExpression, BooleanExpression onClause) {
        super(
            Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null."),
            Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.")
        );
        this.operator = Objects.requireNonNull(operator, "The parameter 'operator' cannot be null.");
        this.onClause = onClause;
    }

    public JoinOperator getOperator() {
        return operator;
    }

    public BooleanExpression getOnClause() {
        return onClause;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OnJoin onJoin = (OnJoin) o;

        if (!getLeftExpression().equals(onJoin.getLeftExpression())) return false;
        if (!getRightExpression().equals(onJoin.getRightExpression())) return false;
        if (operator != onJoin.operator) return false;
        return onClause != null ? onClause.equals(onJoin.onClause) : onJoin.onClause == null;
    }

    @Override
    public int hashCode() {
        int result = getLeftExpression().hashCode();
        result = 31 * result + getRightExpression().hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + (onClause != null ? onClause.hashCode() : 0);
        return result;
    }

}
