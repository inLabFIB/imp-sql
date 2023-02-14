package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class Assertion implements SQLObjectSchemaEntity {

    private String name;
    private BooleanExpression booleanExpression;


    @Override
    public String visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
