package edu.upc.imp.sqlobjectschema.visitor;

public interface SQLObjectSchemaEntity {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
