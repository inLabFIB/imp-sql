package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLDataType;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Attribute implements SQLObjectSchemaEntity {

    private final String attributeName;
    private final SQLDataType type;
    private final boolean nullable;
    private final ValueExpression defaultExpression;

    public Attribute(String attributeName, SQLDataType type) {
        this(attributeName, type, true, null);
    }
    public Attribute(String attributeName, SQLDataType type, boolean nullable) {
        this(attributeName, type, nullable, null);
    }
    public Attribute(String attributeName, SQLDataType type, ValueExpression defaultExpression){
        this(attributeName, type, true, defaultExpression);
    }
    public Attribute(String attributeName, SQLDataType type, boolean nullable, ValueExpression defaultExpression) {
        this.attributeName = Objects.requireNonNull(attributeName, "The parameter 'attributeName' cannot be null.");
        this.type = Objects.requireNonNull(type, "The parameter 'type' cannot be null.");
        this.nullable = nullable;
        this.defaultExpression = defaultExpression;
    }

    public String getName() {
        return attributeName;
    }

    public SQLDataType getType() {
        return type;
    }

    public boolean isNotNull() {
        return !nullable;
    }

    public boolean hasDefaultExpression() {
        return defaultExpression != null;
    }
    public ValueExpression getDefaultExpression() {
        return defaultExpression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Attribute a
            && attributeName.equals(a.attributeName)
            && type.equals(a.type)
            && nullable == a.nullable
            && Objects.equals(defaultExpression, a.defaultExpression);
    }
}
