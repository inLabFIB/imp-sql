package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface SelectItem extends SQLObjectSchemaEntity {

    String getColumAlias();
}
