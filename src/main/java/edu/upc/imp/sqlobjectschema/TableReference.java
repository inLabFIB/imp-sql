package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class TableReference extends AliasableRelationalExpression {

    private final FullTableName fullTableName;

    public TableReference(FullTableName tableName, String alias) {
        super(alias);
        this.fullTableName = Objects.requireNonNull(tableName, "The parameter 'tableName' cannot be null.");
    }

    public TableReference(FullTableName tableName) {
        this(tableName, null);
    }

    public FullTableName getFullTableName() {
        return fullTableName;
    }

    public String getTableName() {
        return fullTableName.getTableName();
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableReference(fullTableName, newAlias);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof TableReference t
            && Objects.equals(getAlias(), t.getAlias())
            && fullTableName.equals(t.fullTableName);
    }
}
