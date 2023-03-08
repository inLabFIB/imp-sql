package edu.upc.imp.sqlobjectschema.visitor;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;

public interface SQLObjectSchemaEntity {
    <T> T visit(SQLObjectSchemaVisitor visitor);
}
