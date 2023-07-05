package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * Has precision (The maximum total number of decimal digits to be stored) and
 * scale (The number of decimal digits that are stored to the right of the decimal point) values.
 */
public class SQLDecimal implements SQLDataType {
    private final Integer precision;
    private final Integer scale;
    public SQLDecimal() {
        this(18, 0);
    }

    public SQLDecimal(Integer precision) {
        this(precision, 0);
    }

    public SQLDecimal(Integer precision, Integer scale) {
        this.precision = Objects.requireNonNull(precision, "A precision value must be set for SQLNumeric dataType");
        this.scale = Objects.requireNonNull(scale, "A scale value must be set for SQLNumeric dataType");
    }


    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    public Integer getPrecision() {
        return precision;
    }

    public Integer getScale() {
        return scale;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLDecimal that = (SQLDecimal) o;

        if (!precision.equals(that.precision)) return false;
        return scale.equals(that.scale);
    }

    @Override
    public int hashCode() {
        int result = precision.hashCode();
        result = 31 * result + scale.hashCode();
        return result;
    }
}
