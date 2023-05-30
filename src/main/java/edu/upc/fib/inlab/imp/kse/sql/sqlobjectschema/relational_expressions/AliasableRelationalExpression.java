package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import java.util.Objects;

public abstract class AliasableRelationalExpression implements RelationalExpression {

    private final String alias;

    protected AliasableRelationalExpression(String alias) {
        this.alias = alias;
    }

    public abstract AliasableRelationalExpression getAliasedCopy(String newAlias);

    public String getAlias() {
        return alias;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AliasableRelationalExpression that = (AliasableRelationalExpression) o;

        return Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return alias != null ? alias.hashCode() : 0;
    }
}
