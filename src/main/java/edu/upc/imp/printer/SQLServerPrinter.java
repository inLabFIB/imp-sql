package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

@SuppressWarnings("unchecked")
public class SQLServerPrinter implements SQLObjectSchemaVisitor {

    @Override
    public String visit(TableExpression te) {
        StringBuilder subquery = new StringBuilder("SELECT ");
        for (int i = 0; i < te.getNumberOfSelectClauseItems(); i++) {
            if (i > 0) subquery.append(", ");
            ValueExpression value = te.getNthSelectionValue(i);
            String alias = te.getNthSelectionAlias(i);
            if (alias == null) subquery.append(value.<String>visit(this));
            else subquery.append(value.<String>visit(this)).append(" AS ").append(alias);
        }

        RelationalExpression fromClause = te.getFromClause();
        if (fromClause != null) subquery.append(" FROM ").append(fromClause.<String>visit(this));

        BooleanExpression whereClause = te.getWhereClause();
        if (whereClause != null) subquery.append(" WHERE ").append(whereClause.<String>visit(this));

        if (te.getAlias() == null) return subquery.toString();
        return "(" + subquery + ") AS " + te.getAlias();
    }

    @Override
    public String visit(CrossJoin j) {
        return j.getLeftExpression().<String>visit(this) + " CROSS JOIN " + j.getRightExpression().<String>visit(this);
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
        return j.getLeftExpression().<String>visit(this) + operation + j.getRightExpression().<String>visit(this) +
            " ON (" + j.getOnClause().<String>visit(this) + ")";
    }

    @Override
    public String visit(TableReference tr) {
        if (tr.getAlias() == null) return tr.getTableName();
        return tr.getTableName() + " AS " + tr.getAlias();
    }

    @Override
    public String visit(ComparisonPredicate cp) {
        String operation = switch (cp.getOperator()) {
            case EQ -> " = ";
            default -> " " + cp.getOperator().toString() + " ";
        };
        return cp.getLeftExpression().<String>visit(this) + operation + cp.getRightExpression().<String>visit(this);
    }

    @Override
    public String visit(ColumnReference cr) {
        return cr.getTableName() + "." + cr.getColumnName();
    }

    @Override
    public String visit(PredicateOperation po) {
        String operation = switch (po.getOperator()) {
            case AND -> " AND ";
            default -> " " + po.getOperator().toString() + " ";
        };
        return po.getLeftExpression().<String>visit(this) + operation + po.getRightExpression().<String>visit(this);
    }

    @Override
    public String visit(Assertion a) {
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores
        return "CREATE ASSERTION " + a.getName() + " CHECK ( " + a.getBooleanExpression().<String>visit(this) + " );";
    }

    @Override
    public String visit(View v) {
        if (v.getQuery().getAlias() != null) throw new RuntimeException("Query of View cannot have an alias in TSQL.");
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores
        return "CREATE VIEW " + v.getName() + " AS " + v.getQuery().<String>visit(this) + ";";
    }

    @Override
    public String visit(NotOperation no) {
        return "NOT ( " + no.getExpression().<String>visit(this) + " )";
    }

    @Override
    public String visit(ExistsPredicate ep) {
        if (ep.getQuery().getAlias() != null) throw new RuntimeException("Query inside ExistsPredicate cannot have an alias in TSQL.");
        return "EXISTS ( " + ep.getQuery().<String>visit(this) + " )";
    }

    @Override
    public String visit(SQLInteger i) {
        return "" + i.getValue();
    }

    @Override
    public String visit(SQLFloat f) {
        return "" + f.getValue();
    }

    @Override
    public String visit(SQLString s) {
        return "'" + s.getValue() + "'";
    }
}
