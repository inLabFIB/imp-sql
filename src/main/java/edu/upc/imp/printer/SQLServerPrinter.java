package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

@SuppressWarnings("unchecked")
public class SQLServerPrinter implements SQLObjectSchemaVisitor {

    @Override
    public String visit(TableExpression te) {
        StringBuilder subquery = new StringBuilder("SELECT ");
        subquery.append(String.join(", ", te.getSelectClause().stream().map(s -> s.<String>visit(this)).toList()));

        RelationalExpression fromClause = te.getFromClause();
        if (fromClause != null) subquery.append(" FROM ").append(fromClause.<String>visit(this));

        BooleanExpression whereClause = te.getWhereClause();
        if (whereClause != null) subquery.append(" WHERE ").append(whereClause.<String>visit(this));

        if (te.getAlias() == null) {
            return te.isFirstLevel() ? subquery + ";" : "( " + subquery + " )";
        }
        return "( " + subquery + " ) AS " + te.getAlias();
    }

    /**
     * Only adds parenthesis if the right expression contains another cross join.
     * The left expression does not need parenthesis since it is evaluated first (assuming left to right order).
     * The other cases (with on clauses) are already evaluated first (because of the on clause).
     *  WARNING: Natural join does not have an on clause, when implementing it, add it to the cases where parenthesis are added.
     */
    @Override
    public String visit(CrossJoin j) {
        String rightExp = j.getRightExpression().visit(this);
        if (j.getRightExpression() instanceof CrossJoin) rightExp = "( " + rightExp + " )";
        return j.getLeftExpression().<String>visit(this) + " CROSS JOIN " + rightExp;
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
            " ON ( " + j.getOnClause().<String>visit(this) + " )";
    }

    @Override
    public String visit(TableReference tr) {
        if (tr.getAlias() == null) return tr.getFullTableName().visit(this);
        return tr.getFullTableName().<String>visit(this) + " AS " + tr.getAlias();
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
        if (cr.getFullTableName() == null) return cr.getColumnName();
        return cr.getFullTableName().<String>visit(this) + "." + cr.getColumnName();
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
        return "CREATE ASSERTION " + a.getAssertionName().<String>visit(this) + " CHECK ( " + a.getBooleanExpression().<String>visit(this) + " );";
    }

    @Override
    public String visit(View v) {
        if (v.getQuery().getAlias() != null) throw new RuntimeException("Query of View cannot have an alias in TSQL.");
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores
        String viewCreationStatement = "CREATE VIEW " + v.getViewName().<String>visit(this);
        if (v.getColumnNames() != null && v.getColumnNames().size() > 0) viewCreationStatement += " ( " + String.join(", ", v.getColumnNames()) + " )";
        viewCreationStatement += " AS " + v.getQuery().<String>visit(this) + ";";
        return viewCreationStatement;
    }

    @Override
    public String visit(NotOperation no) {
        return "NOT ( " + no.getExpression().<String>visit(this) + " )";
    }

    @Override
    public String visit(ExistsPredicate ep) {
        if (ep.getQuery().getAlias() != null) throw new RuntimeException("Query inside ExistsPredicate cannot have an alias in TSQL.");
        return "EXISTS " + ep.getQuery().<String>visit(this);
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

    @Override
    public String visit(FullTableName tn) {
        return tn.getFullTableName();
    }

    @Override
    public String visit(Asterisk a) {
        return "*";
    }

    @Override
    public String visit(AliasableSelectItem asi) {
        if (asi.getColumAlias() == null) return asi.getExpression().visit(this);
        return asi.getExpression().<String>visit(this) + " AS " + asi.getColumAlias();
    }
}
