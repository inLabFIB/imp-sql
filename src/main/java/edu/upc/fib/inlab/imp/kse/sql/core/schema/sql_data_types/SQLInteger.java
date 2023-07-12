package edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

public class SQLInteger implements SQLDataType {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SQLInteger;
    }

    @Override
    public int hashCode() {
        return 5;
    }
}
