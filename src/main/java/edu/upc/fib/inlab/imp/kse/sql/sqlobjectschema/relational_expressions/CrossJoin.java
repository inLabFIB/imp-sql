package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class CrossJoin extends JoinOperation {

    public CrossJoin(RelationalExpression leftExpression, RelationalExpression rightExpression) {
        super(
            Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null."),
            Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.")
            );
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

}
