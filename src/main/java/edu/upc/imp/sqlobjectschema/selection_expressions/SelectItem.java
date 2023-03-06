package edu.upc.imp.sqlobjectschema.selection_expressions;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface SelectItem extends SQLObjectSchemaEntity {

    String getColumAlias();
}
