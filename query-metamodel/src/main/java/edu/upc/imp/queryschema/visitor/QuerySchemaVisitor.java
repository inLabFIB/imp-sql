package edu.upc.imp.queryschema.visitor;

import edu.upc.imp.queryschema.*;

public interface QuerySchemaVisitor {
    String visit(TableExpression te);
    String visit(JoinOperation jo);
    String visit(TableReference tr);
    String visit(ComparisonPredicate cp);
    String visit(ColumnReference cr);
    String visit(Constant c);
    String visit(PredicateOperation po);
    String visit(Assertion a);
}
