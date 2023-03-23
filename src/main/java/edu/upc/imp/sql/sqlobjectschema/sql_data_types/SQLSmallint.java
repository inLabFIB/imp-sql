package edu.upc.imp.sql.sqlobjectschema.sql_data_types;

import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class SQLSmallint implements SQLDataType {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLSmallint;
    }
}
