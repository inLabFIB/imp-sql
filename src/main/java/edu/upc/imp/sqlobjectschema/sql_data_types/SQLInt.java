package edu.upc.imp.sqlobjectschema.sql_data_types;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class SQLInt implements SQLDataType {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLInt;
    }
}
