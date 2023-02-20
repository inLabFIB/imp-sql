package edu.upc.imp.sqlobjectschema.visitor;

import edu.upc.imp.sqlobjectschema.*;

public interface SQLObjectSchemaVisitor {
    <T> T visit(TableExpression te);
    <T> T visit(CrossJoin j);
    <T> T visit(OnJoin j);
    <T> T visit(TableReference tr);
    <T> T visit(ComparisonPredicate cp);
    <T> T visit(ColumnReference cr);
    <T> T visit(PredicateOperation po);
    <T> T visit(Assertion a);
    <T> T visit(View v);
    <T> T visit(NotOperation no);
    <T> T visit(ExistsPredicate ep);
    <T> T visit(SQLInteger d);
    <T> T visit(SQLFloat f);
    <T> T visit(SQLString s);
    <T> T visit(FullTableName tn);
    <T> T visit(Asterisk a);
    <T> T visit(AliasableSelectItem asi);
}
