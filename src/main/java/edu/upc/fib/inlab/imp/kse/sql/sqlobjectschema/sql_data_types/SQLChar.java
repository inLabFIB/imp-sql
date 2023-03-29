package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLChar sqlChar = (SQLChar) o;

        return length != null ? length.equals(sqlChar.length) : sqlChar.length == null;
    }

    @Override
    public int hashCode() {
        return length != null ? length.hashCode() : 0;
    }
}
