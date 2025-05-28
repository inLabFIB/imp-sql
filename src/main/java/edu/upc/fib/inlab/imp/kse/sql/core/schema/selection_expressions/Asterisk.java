package edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

public class Asterisk implements SelectItem {
    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Asterisk;
    }
}
