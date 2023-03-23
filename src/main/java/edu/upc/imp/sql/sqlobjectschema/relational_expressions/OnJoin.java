package edu.upc.imp.sql.sqlobjectschema.relational_expressions;

import edu.upc.imp.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof OnJoin oj
            && operator.equals(oj.operator)
            && getLeftExpression().equals(oj.getLeftExpression())
            && getRightExpression().equals(oj.getRightExpression())
            && Objects.equals(onClause, oj.onClause);
    }
}
