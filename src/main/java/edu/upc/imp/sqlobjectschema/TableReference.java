package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class TableReference extends AliasableRelationalExpression {

    private final FullTableName tableName;

    public TableReference(FullTableName tableName, String alias) {
        super(alias);
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName.getTableName();
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableReference(tableName, newAlias);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof TableReference t
            && Objects.equals(getAlias(), t.getAlias())
            && tableName.equals(t.tableName);
    }
}
