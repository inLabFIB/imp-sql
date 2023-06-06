package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaValueObject;

public interface SQLDataType extends SQLObjectSchemaValueObject {
    enum Type {
        CHAR,
        VARCHAR,
        BIT,
        NUMERIC,
        INT,
        SMALLINT,
        FLOAT,
        REAL,
        DATE,
        DATETIME
    }
}
