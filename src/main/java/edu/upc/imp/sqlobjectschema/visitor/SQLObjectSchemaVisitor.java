package edu.upc.imp.sqlobjectschema.visitor;

import edu.upc.imp.sqlobjectschema.*;

public interface SQLObjectSchemaVisitor {
    String visit(TableExpression te);
    String visit(JoinOperation jo);
    String visit(TableReference tr);
    String visit(ComparisonPredicate cp);
    String visit(ColumnReference cr);
    String visit(Constant c);
    String visit(PredicateOperation po);
    String visit(Assertion a);
    String visit(View v);
}
