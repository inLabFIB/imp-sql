package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLCharacter implements SQLDataType {

    private final Integer length;

    public SQLCharacter() {
        this(null);
    }

    public SQLCharacter(Integer length) {
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

        SQLCharacter sqlChar = (SQLCharacter) o;

        return Objects.equals(length, sqlChar.length);
    }

    @Override
    public int hashCode() {
        return length != null ? length.hashCode() : 0;
    }
}
