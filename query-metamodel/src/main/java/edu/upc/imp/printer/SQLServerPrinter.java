package edu.upc.imp.printer;

import edu.upc.imp.queryschema.*;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

public class SQLServerPrinter implements QuerySchemaVisitor {

    @Override
    public String visit(Query q) {
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
}
