package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class SQLDate implements SQLDataType {
    private final Integer precision;

    public SQLDate() {
        this(null);
    }

    public SQLDate(Integer precision) {
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

        SQLDate sqlDate = (SQLDate) o;

        return precision != null ? precision.equals(sqlDate.precision) : sqlDate.precision == null;
    }

    @Override
    public int hashCode() {
        return precision != null ? precision.hashCode() : 0;
    }
}
