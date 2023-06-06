package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaValueObject;

import java.util.List;

public interface RelationalExpression extends SQLObjectSchemaValueObject {
    List<ColumnReference> getOfferedReferences();
}
