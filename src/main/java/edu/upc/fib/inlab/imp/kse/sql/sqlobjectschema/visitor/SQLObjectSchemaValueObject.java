package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor;

public interface SQLObjectSchemaValueObject {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
