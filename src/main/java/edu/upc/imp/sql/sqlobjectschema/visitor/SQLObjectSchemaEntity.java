package edu.upc.imp.sql.sqlobjectschema.visitor;

public interface SQLObjectSchemaEntity {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
