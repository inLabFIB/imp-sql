package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaValueObject;

public interface ValueExpression extends SQLObjectSchemaValueObject {
    String computeDefaultColumnAlias();
}
