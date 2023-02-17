package edu.upc.imp.sqlobjectschema;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {
    protected Query(String alias) {
        super(alias);
    }
}
