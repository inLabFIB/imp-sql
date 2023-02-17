package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class TableReference extends AliasableRelationalExpression {

    private final String name;

    public TableReference(String name, String alias) {
        super(alias);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
