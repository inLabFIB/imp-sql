package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class Assertion implements SQLObjectSchemaEntity {

    private final FullTableName assertionName;
    private final BooleanExpression booleanExpression;

    public Assertion(FullTableName name, BooleanExpression booleanExpression) {
        this.assertionName = name;
        this.booleanExpression = booleanExpression;
    }


    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    public String getAssertionName() {
        return assertionName.getTableName();
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }
}
