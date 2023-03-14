package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.boolean_expressions.*;
import edu.upc.imp.sqlobjectschema.constraints.*;
import edu.upc.imp.sqlobjectschema.relational_expressions.*;
import edu.upc.imp.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.imp.sqlobjectschema.selection_expressions.Asterisk;
import edu.upc.imp.sqlobjectschema.sql_data_types.*;
import edu.upc.imp.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveFloat;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveString;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.List;

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

        if (te.getAlias() == null) return "( " + subquery + " )";
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
        String tableReference;
        SchemaReference schemaReference = tr.getTable().getSchemaReference();
        if (schemaReference == null) tableReference = tr.getTable().getTableName();
        else tableReference = schemaReference.<String>visit(this) + "." + tr.getTable().getTableName();

        if (tr.getAlias() != null) return tableReference + " AS " + tr.getAlias();
        return tableReference;
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
        String columnName = cr.getColumnName();
        String tableName = cr.getTableName();
        return tableName == null ? columnName : tableName + "." + columnName;
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

        String assertionName = (a.getSchemaReference() != null) ? a.getSchemaReference().visit(this) : "";
        assertionName += a.getAssertionName();

        return "CREATE ASSERTION " + assertionName + " CHECK ( " + a.getBooleanExpression().<String>visit(this) + " );";
    }

    @Override
    public String visit(View v) {
        if (v.getQuery().getAlias() != null) throw new RuntimeException("Query of View cannot have an alias in TSQL.");
        // TODO: Ensure that the name is returned in a valid TSQL format by doing any necessary modifications.
        //  e.g. replace whitespaces with underscores

        String viewName = (v.getSchemaReference() != null) ? v.getSchemaReference().visit(this) : "";
        viewName += v.getViewName();

        String viewCreationStatement = "CREATE VIEW " + viewName;
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
    public String visit(SQLPrimitiveInteger i) {
        return "" + i.getValue();
    }

    @Override
    public String visit(SQLPrimitiveFloat f) {
        return "" + f.getValue();
    }

    @Override
    public String visit(SQLPrimitiveString s) {
        return "'" + s.getValue() + "'";
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

    @Override
    public String visit(SchemaReference sr) {
        String fullName = "";
        if (sr.getServerName() != null) fullName += sr.getServerName() + ".";
        if (sr.getDatabaseName() != null) fullName += sr.getDatabaseName() + ".";
        else if (sr.getServerName() != null) fullName += ".";
        fullName += sr.getSchemaName();
        return fullName;
    }

    @Override
    public String visit(Table t) {
        String prefix = "";
        if (t.getSchemaReference() != null) prefix = t.getSchemaReference().visit(this)+".";
        List<TableConstraint> tableConstraints = t.getTableConstraints();
        String constraints = "";
        if (!tableConstraints.isEmpty()) constraints = ", " +
            String.join(", ", tableConstraints.stream().map(c -> c.<String>visit(this)).toList());
        return "CREATE TABLE " + prefix + t.getTableName() + " ( "
            + String.join(", ", t.getAttributes().stream().map(a -> a.<String>visit(this)).toList())
            + constraints + " );";
    }

    @Override
    public String visit(Attribute a) {
        return a.getName() + " " + a.getType().<String>visit(this) +
            (a.hasDefaultExpression() ? " DEFAULT " + a.getDefaultExpression().<String>visit(this) : "") +
            (a.isNotNull() ? " NOT NULL" : "");
    }

    @Override
    public String visit(Check c) {
        String checkCreationStatement = "";
        if (c.hasName()) checkCreationStatement = "CONSTRAINT " + c.getName() + " ";
        checkCreationStatement += "CHECK (" +
            c.getExpression().<String>visit(this) +
            ")";
        return checkCreationStatement;
    }

    @Override
    public String visit(Unique u) {
        String uniqueCreationStatement = "";
        if (u.hasName()) uniqueCreationStatement = "CONSTRAINT " + u.getName() + " ";
        uniqueCreationStatement += "UNIQUE (" +
            String.join(", ", u.getAttributes().stream().map(Attribute::getName).toList())
            + ")";
        return uniqueCreationStatement;
    }

    @Override
    public String visit(PrimaryKey pk) {
        String pkCreationStatement = "";
        if (pk.hasName()) pkCreationStatement = "CONSTRAINT " + pk.getName() + " ";
        pkCreationStatement += "PRIMARY KEY (" +
            String.join(", ", pk.getPkAttributes().stream().map(Attribute::getName).toList())
            + ")";
        return pkCreationStatement;
    }

    @Override
    public String visit(ForeignKey fk) {
        String prefix = "";
        if (fk.getPkReferenceTable().getSchemaReference() != null)
            prefix = fk.getPkReferenceTable().getSchemaReference().visit(this)+".";
        String fkCreationStatement = "";
        if (fk.hasName()) fkCreationStatement = "CONSTRAINT " + fk.getName() + " ";
        fkCreationStatement += "FOREIGN KEY (" +
            String.join(", ", fk.getFkAttributes().stream().map(Attribute::getName).toList());
        fkCreationStatement += ") REFERENCES " + prefix + fk.getPkReferenceTable().getTableName() + " ("+
            String.join(", ", fk.getPkReference().stream().map(Attribute::getName).toList())
            + ")";
        return fkCreationStatement;
    }

    @Override
    public String visit(SQLChar c) {
        if (c.getLength() != null) return "CHAR(" + c.getLength() + ")";
        return "CHAR";
    }

    @Override
    public String visit(SQLVarchar v) {
        return "VARCHAR(" + v.getLength() + ")";
    }

    @Override
    public String visit(SQLBit b) {
        return "BIT";
    }

    @Override
    public String visit(SQLInt i) {
        return "INT";
    }

    @Override
    public String visit(SQLSmallint s) {
        return "SMALLINT";
    }

    @Override
    public String visit(SQLFloat f) {
        if (f.getPrecision() != null) return "FLOAT(" + f.getPrecision() + ")";
        return "FLOAT";
    }

    @Override
    public String visit(SQLReal r) {
        return "REAL";
    }

    @Override
    public String visit(SQLDate d) {
        if (d.getPrecision() != null) return "DATETIME2(" + d.getPrecision() + ")";
        return "DATETIME2";
    }

    @Override
    public String visit(SQLDoublePrecision dp) {
        return "DOUBLE PRECISION";
    }
}
