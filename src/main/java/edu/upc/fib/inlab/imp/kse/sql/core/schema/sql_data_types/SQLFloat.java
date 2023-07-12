package edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLFloat implements SQLDataType {
    private final Integer precision;

    public SQLFloat() {
        this(null);
    }

    public SQLFloat(Integer precision) {
        this.precision = precision;
    }

    public Integer getPrecision() {
        return precision;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLFloat sqlFloat = (SQLFloat) o;

        return Objects.equals(precision, sqlFloat.precision);
    }

    @Override
    public int hashCode() {
        return precision != null ? precision.hashCode() : 0;
    }
}
