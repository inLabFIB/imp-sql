package edu.upc.imp.sql.sqlobjectschema.selection_expressions;

import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface SelectItem extends SQLObjectSchemaEntity {

    String getColumAlias();
}
