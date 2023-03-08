package edu.upc.imp.sqlobjectschema.relational_expressions;

import edu.upc.imp.sqlobjectschema.value_expressions.ValueExpression;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {

    protected Query(String alias) {
        super(alias);
    }

}
