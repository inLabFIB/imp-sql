package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;

import java.util.List;

public interface RelationalExpression extends SQLObjectSchemaEntity {
    List<ColumnReference> getOfferedReferences();
}
