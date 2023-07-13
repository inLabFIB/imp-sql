package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ValueExpression;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {

    protected Query(String alias) {
        super(alias);
    }

}
