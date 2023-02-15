package edu.upc.imp.sqlobjectschema.visitor;

import edu.upc.imp.sqlobjectschema.*;

public interface SQLObjectSchemaVisitor {
    <T> T visit(TableExpression te);
    <T> T visit(JoinOperation jo);
    <T> T visit(TableReference tr);
    <T> T visit(ComparisonPredicate cp);
    <T> T visit(ColumnReference cr);
    <T> T visit(Constant c);
    <T> T visit(PredicateOperation po);
    <T> T visit(Assertion a);
    <T> T visit(View v);
}
