package edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLBit implements SQLDataType {
    private final Integer length;

    public SQLBit() {
        this(null);
    }

    public SQLBit(Integer length) {
        this.length = length;
    }

    public Integer getLength() {
        return length;
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

        return Objects.equals(length, sqlBit.length);
    }

    @Override
    public int hashCode() {
        return length != null ? length.hashCode() : 0;
    }
}
