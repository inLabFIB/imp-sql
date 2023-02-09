package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class Assertion implements QuerySchemaObject {

    private String name;
    private BooleanExpression booleanExpression;


    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
