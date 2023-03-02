package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Attribute implements SQLObjectSchemaEntity {

    //TODO: extend with official data types
    public enum SQLDataType {
        CHAR, VARCHAR,
        BOOL, INT, FLOAT,
        DATE;
    }

    private final String attributeName;
    private final SQLDataType type;
    private final int bytes;
    private final boolean nullable;

    public Attribute(String attributeName, SQLDataType type, int bytes, boolean nullable) {
        this.attributeName = Objects.requireNonNull(attributeName, "The parameter 'attributeName' cannot be null.");
        this.type = Objects.requireNonNull(type, "The parameter 'type' cannot be null.");
        this.bytes = bytes;
        this.nullable = nullable;
    }
    public String getName() {
        return attributeName;
    }

    public SQLDataType getType() {
        return type;
    }

    public int getBytes() {
        return bytes;
    }

    public boolean isNotNull() {
        return !nullable;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }


}
