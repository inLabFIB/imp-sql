package edu.upc.imp.sqlobjectschema.relational_expressions;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof CrossJoin cj
            && getLeftExpression().equals(cj.getLeftExpression())
            && getRightExpression().equals(cj.getRightExpression());
    }
}
