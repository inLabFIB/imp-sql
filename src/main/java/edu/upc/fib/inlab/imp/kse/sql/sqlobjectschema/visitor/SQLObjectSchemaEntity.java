package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor;

public interface SQLObjectSchemaEntity {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
