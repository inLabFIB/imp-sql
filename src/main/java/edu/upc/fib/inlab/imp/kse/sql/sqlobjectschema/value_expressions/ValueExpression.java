package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;

public interface ValueExpression extends SQLObjectSchemaEntity {
    String computeDefaultColumnAlias();
}
