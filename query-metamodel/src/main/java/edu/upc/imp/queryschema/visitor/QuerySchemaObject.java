package edu.upc.imp.queryschema.visitor;

public interface QuerySchemaObject {
    void visit(QuerySchemaVisitor visitor);
}
