package edu.upc.imp.sqlobjectschema.sql_data_types;

import edu.upc.imp.sqlobjectschema.SQLPrimitiveFloat;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLChar implements SQLDataType {

    private final Integer length;

    public SQLChar() {
        this(null);
    }

    public SQLChar(Integer length) {
        this.length = length;
    }

    public Integer getLength() {
        return length;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLChar c
            && Objects.equals(length, c.length);
    }
}
