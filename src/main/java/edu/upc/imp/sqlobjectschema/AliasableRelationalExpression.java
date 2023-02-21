package edu.upc.imp.sqlobjectschema;

public abstract class AliasableRelationalExpression implements RelationalExpression {
    private final String alias;

    protected AliasableRelationalExpression(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public abstract AliasableRelationalExpression getAliasedCopy(String newAlias);
}
