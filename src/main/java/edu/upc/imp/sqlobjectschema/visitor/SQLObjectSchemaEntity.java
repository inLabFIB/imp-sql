package edu.upc.imp.sqlobjectschema.visitor;

public interface SQLObjectSchemaEntity {
    String visit(SQLObjectSchemaVisitor visitor);
}
