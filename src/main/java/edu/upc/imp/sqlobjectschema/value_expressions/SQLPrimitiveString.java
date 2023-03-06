package edu.upc.imp.sqlobjectschema.value_expressions;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SQLPrimitiveString implements PrimitiveConstant {

    private final String value;

    public SQLPrimitiveString(String value) {
        this.value = Objects.requireNonNull(value, "The parameter 'value' cannot be null.");
    }

    public String getValue() {
        return value;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLPrimitiveString s
            && value.equals(s.value);
    }
}
