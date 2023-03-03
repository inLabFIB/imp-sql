package edu.upc.imp.sqlobjectschema.sql_data_types;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLBit implements SQLDataType {
    private final Integer length;

    public SQLBit(){
        this(null);
    }

    public SQLBit(Integer length) {
        this.length = length;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLBit b
            && Objects.equals(length, b.length);
    }
}
