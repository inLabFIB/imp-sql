package edu.upc.imp.sqlobjectschema.sql_data_types;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface SQLDataType extends SQLObjectSchemaEntity {
    public enum Type {
        CHAR,
        VARCHAR,
        BIT,
        INT,
        SMALLINT,
        FLOAT,
        REAL,
        DATE;
    }
}
