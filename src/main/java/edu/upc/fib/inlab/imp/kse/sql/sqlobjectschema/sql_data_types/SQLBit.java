package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class SQLBit implements SQLDataType {
    private final Integer length;

    public SQLBit() {
        this(null);
    }

    public SQLBit(Integer length) {
        this.length = length;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLBit sqlBit = (SQLBit) o;

        return length != null ? length.equals(sqlBit.length) : sqlBit.length == null;
    }

    @Override
    public int hashCode() {
        return length != null ? length.hashCode() : 0;
    }
}
