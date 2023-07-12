package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;

import java.util.List;

public interface RelationalExpression extends SQLObjectSchemaValueObject {
    List<ColumnReference> getOfferedReferences();
}
