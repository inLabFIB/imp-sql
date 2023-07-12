package edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor;

public interface SQLObjectSchemaValueObject {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
