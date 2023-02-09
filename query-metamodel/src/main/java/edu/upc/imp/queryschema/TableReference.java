package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class TableReference implements RelationalExpression, QuerySchemaObject {

    private String name;

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
