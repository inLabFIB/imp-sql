package edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

public class SQLVarchar implements SQLDataType {
    private final int length;

    public SQLVarchar(int length) {
        this.length = length;
    }

    public int getLength() {
        return length;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLVarchar that = (SQLVarchar) o;

        return length == that.length;
    }

    @Override
    public int hashCode() {
        return length;
    }
}
