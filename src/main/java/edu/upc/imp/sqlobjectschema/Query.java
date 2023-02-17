package edu.upc.imp.sqlobjectschema;

public abstract class Query extends AliasableRelationalExpression {
    protected Query(String alias) {
        super(alias);
    }
}
