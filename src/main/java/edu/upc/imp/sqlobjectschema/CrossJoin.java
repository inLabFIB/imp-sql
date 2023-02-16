package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class CrossJoin extends JoinOperation {

    public CrossJoin(RelationalExpression leftExpression, RelationalExpression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
