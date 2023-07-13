package edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

public class SQLDoublePrecision implements SQLDataType {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SQLDoublePrecision;
    }

    @Override
    public int hashCode() {
        return 7;
    }
}
