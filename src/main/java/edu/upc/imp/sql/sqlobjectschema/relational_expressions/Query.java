package edu.upc.imp.sql.sqlobjectschema.relational_expressions;

import edu.upc.imp.sql.sqlobjectschema.value_expressions.ValueExpression;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {

    protected Query(String alias) {
        super(alias);
    }

}
