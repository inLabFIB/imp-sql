package edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

/**
 * This DataType stores can store a Date + Time like YYYY:MM:DD - HH:MM:SS:...
 * fractionalSecondsPrecision is for when time is stored.
 */
public class SQLDate implements SQLDataType {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof SQLDate;
    }

    @Override
    public int hashCode() {
        return 11;
    }
}
