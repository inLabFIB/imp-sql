package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

@SuppressWarnings("unchecked")
public class SQLServerPrinter implements SQLObjectSchemaVisitor {

    private SQLObjectSchema sqlObjectSchema;

    @Override
    public String visit(TableExpression te) {
        return null;
    }

    @Override
    public String visit(JoinOperation jo) {
        return null;
    }

    @Override
    public String visit(TableReference tr) {
        return null;
    }

    @Override
    public String visit(ComparisonPredicate cp) {
        return null;
    }

    @Override
    public String visit(ColumnReference cr) {
        return null;
    }

    @Override
    public String visit(Constant c) {
        return null;
    }

    @Override
    public String visit(PredicateOperation po) {
        return null;
    }

    @Override
    public String visit(Assertion a) {
        return null;
    }

    @Override
    public String visit(View v) {
        return null;
    }
}
