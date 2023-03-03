package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.sql_data_types.SQLDataType;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLVarchar;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Attribute implements SQLObjectSchemaEntity {

    private final String attributeName;
    private final SQLDataType type;
    private final boolean nullable;

    public Attribute(String attributeName, SQLDataType type) {
        this(attributeName, type, true);
    }

    public Attribute(String attributeName, SQLDataType type, boolean nullable) {
        this.attributeName = Objects.requireNonNull(attributeName, "The parameter 'attributeName' cannot be null.");
        this.type = Objects.requireNonNull(type, "The parameter 'type' cannot be null.");
        this.nullable = nullable;
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

    public Attribute getNotNullCopy() {
        return new Attribute(attributeName, type, false);
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
            && nullable == a.nullable;
    }
}
