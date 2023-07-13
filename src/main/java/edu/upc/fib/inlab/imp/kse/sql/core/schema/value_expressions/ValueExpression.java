package edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;

public interface ValueExpression extends SQLObjectSchemaValueObject {
    String computeDefaultColumnAlias();
}
