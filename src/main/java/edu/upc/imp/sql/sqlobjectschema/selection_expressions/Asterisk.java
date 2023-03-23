package edu.upc.imp.sql.sqlobjectschema.selection_expressions;

import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class Asterisk implements SelectItem {

    @Override
    public String getColumAlias() {
        return null;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Asterisk;
    }
}
