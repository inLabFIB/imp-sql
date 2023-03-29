package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class Asterisk implements SelectItem {

    @Override
    public String getColumAlias() {
        return null;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Asterisk;
    }
}
