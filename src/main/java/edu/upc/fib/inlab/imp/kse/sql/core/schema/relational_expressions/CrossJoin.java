package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class CrossJoin extends JoinOperation {

    public CrossJoin(RelationalExpression leftExpression, RelationalExpression rightExpression) {
        super(
            Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null."),
            Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.")
            );
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
