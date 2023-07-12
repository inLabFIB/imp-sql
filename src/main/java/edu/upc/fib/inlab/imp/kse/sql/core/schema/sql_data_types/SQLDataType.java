package edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;

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
