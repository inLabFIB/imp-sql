package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class Constant implements ValueExpression, QuerySchemaObject {

    //TODO: think how to differentiate different types and store values

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
