package edu.upc.imp.sqlobjectschema.relational_expressions;

import edu.upc.imp.sqlobjectschema.value_expressions.ValueExpression;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {

    private final boolean isFirstLevel;

    protected Query(String alias, boolean isFirstLevel) {
        super(alias);
        this.isFirstLevel = isFirstLevel;
    }

    public boolean isFirstLevel() {
        return isFirstLevel;
    }
}
