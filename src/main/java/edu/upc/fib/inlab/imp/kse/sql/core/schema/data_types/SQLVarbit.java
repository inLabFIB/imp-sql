package edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLVarbit implements SQLDataType {
    private final Integer length;

    public SQLVarbit() {
        this(null);
    }

    public SQLVarbit(Integer length) {
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

        SQLVarbit sqlVarbit = (SQLVarbit) o;

        return Objects.equals(length, sqlVarbit.length);
    }

    @Override
    public int hashCode() {
        return length != null ? length.hashCode() : 0;
    }
}
