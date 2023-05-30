package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;

import java.util.List;

public abstract class Query extends AliasableRelationalExpression implements ValueExpression {

    protected Query(String alias) {
        super(alias);
    }

    public abstract List<SelectItem> getSelectClause();


}
