package edu.upc.imp.sql.sqlobjectschema.sql_data_types;

import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLFloat f
            && Objects.equals(precision, f.precision);
    }
}
