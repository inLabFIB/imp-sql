package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class PredicateOperation implements BooleanExpression, SQLObjectSchemaEntity {

    enum PredicateOperator {
        AND
    }

    private PredicateOperator operator;
    private BooleanExpression leftExpression;
    private BooleanExpression rightExpression;

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
