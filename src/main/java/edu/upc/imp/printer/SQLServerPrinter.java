package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

@SuppressWarnings("unchecked")
public class SQLServerPrinter implements SQLObjectSchemaVisitor {

    @Override
    public String visit(TableExpression te) {
        return null;
    }

    @Override
    public String visit(CrossJoin j) {
        return j.getLeftExpression().visit(this) + " CROSS JOIN " + j.getRightExpression().visit(this);
    }

    @Override
    public String visit(OnJoin j) {
        String operation = switch (j.getOperator()) {
            case INNER -> " INNER JOIN ";
            case NATURAL -> " NATURAL JOIN ";
            case LEFT -> " LEFT OUTER JOIN ";
            case RIGHT -> " RIGHT OUTER JOIN ";
            case FULL -> " FULL OUTER JOIN ";
            default -> " " + j.getOperator().toString() + " ";
        };
        return j.getLeftExpression().visit(this) + operation + j.getRightExpression().visit(this) +
            " ON (" + j.getOnClause().visit(this) + ")";
    }

    @Override
    public String visit(TableReference tr) {
        return tr.getName();
    }

    @Override
    public String visit(ComparisonPredicate cp) {
        String operation = switch (cp.getOperator()) {
            case EQ -> " = ";
            default -> " " + cp.getOperator().toString() + " ";
        };
        return cp.getLeftExpression().visit(this) + operation + cp.getRightExpression().visit(this);
    }

    @Override
    public String visit(ColumnReference cr) {
        return cr.getTableAlias() + "." + cr.getColumnAlias();
    }

    @Override
    public String visit(Constant c) {
        // TODO: Change this when we treat subclasses of constant
        return c.getValue();
    }

    @Override
    public String visit(PredicateOperation po) {
        String operation = switch (po.getOperator()) {
            case AND -> " AND ";
            default -> " " + po.getOperator().toString() + " ";
        };
        return po.getLeftExpression().visit(this) + operation + po.getRightExpression().visit(this);
    }

    @Override
    public String visit(Assertion a) {
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores
        return "CREATE ASSERTION " + a.getName() + " CHECK ( " + a.getBooleanExpression().visit(this) + " );";
    }

    @Override
    public String visit(View v) {
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores
        return "CREATE VIEW " + v.getName() + " AS " + v.getQuery().visit(this);
    }
}
