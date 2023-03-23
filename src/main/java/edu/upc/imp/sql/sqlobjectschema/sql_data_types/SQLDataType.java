package edu.upc.imp.sql.sqlobjectschema.sql_data_types;

import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface SQLDataType extends SQLObjectSchemaEntity {
    enum Type {
        CHAR,
        VARCHAR,
        BIT,
        INT,
        SMALLINT,
        FLOAT,
        REAL,
        DATE
    }
}
