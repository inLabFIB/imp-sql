package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.SQLDataType;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ValueExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Attribute implements SQLObjectSchemaValueObject {

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
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Attribute attribute = (Attribute) o;

        if (nullable != attribute.nullable) return false;
        if (!attributeName.equals(attribute.attributeName)) return false;
        if (!type.equals(attribute.type)) return false;
        return Objects.equals(defaultExpression, attribute.defaultExpression);
    }

    @Override
    public int hashCode() {
        int result = attributeName.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (nullable ? 1 : 0);
        result = 31 * result + (defaultExpression != null ? defaultExpression.hashCode() : 0);
        return result;
    }

}
