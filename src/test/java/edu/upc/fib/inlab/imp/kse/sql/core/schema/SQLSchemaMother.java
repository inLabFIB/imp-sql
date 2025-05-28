package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ValueExpression;

public class SQLSchemaMother {

    public static AliasableSelectItem createAliasableSelectItem(ValueExpression expression) {
        return new AliasableSelectItem(expression, null);
    }
}
