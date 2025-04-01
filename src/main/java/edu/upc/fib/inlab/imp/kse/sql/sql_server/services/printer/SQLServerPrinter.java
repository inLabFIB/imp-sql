package edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.CrossJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.OnJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.RelationalExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.SetOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.services.printer.SQLPrinter;

import java.util.List;
import java.util.stream.Collectors;

public class SQLServerPrinter extends SQLPrinter {

    @Override
    public String visit(TableExpression te) {
        StringBuilder subquery = new StringBuilder("SELECT ");
        subquery.append(String.join(", ", te.getSelectClause().stream().map(s -> s.visit(this)).toList()));

        RelationalExpression fromClause = te.getFromClause();
        if (fromClause != null) subquery.append(" FROM ").append(fromClause.visit(this));

        BooleanExpression whereClause = te.getWhereClause();
        if (whereClause != null) subquery.append(" WHERE ").append(whereClause.visit(this));

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
        return j.getLeftExpression().visit(this) + " CROSS JOIN " + rightExp;
    }

    @Override
    public String visit(OnJoin j) {
        String operation = switch (j.getOperator()) {
            case INNER -> " INNER JOIN ";
            case NATURAL -> " NATURAL JOIN ";
            case LEFT -> " LEFT OUTER JOIN ";
            case RIGHT -> " RIGHT OUTER JOIN ";
            case FULL -> " FULL OUTER JOIN ";
        };
        return j.getLeftExpression().visit(this) + operation + j.getRightExpression().visit(this) +
            " ON ( " + j.getOnClause().visit(this) + " )";
    }

    @Override
    public String visit(SetOperation so) {
        String operator = switch (so.getOperator()) {
            case UNION -> " UNION ";
            case EXCEPT -> " EXCEPT ";
            case INTERSECT -> " INTERSECT ";
        };

        if (so.returnsDuplicates()) operator += "ALL ";
        String union = so.getLeftExpression().visit(this) + operator + so.getRightExpression().visit(this);

        if (so.getAlias() != null) return "( " + union + " ) AS " + so.getAlias();
        return "( " + union + " )";
    }

    @Override
    public String visit(TableReference tr) {
        String tableReference;
        SchemaReference schemaReference = tr.getTableSource().getSchemaReference();
        if (schemaReference == null) tableReference = tr.getTableSource().getName();
        else tableReference = schemaReference.visit(this) + "." + tr.getTableSource().getName();

        if (tr.getAlias() != null) return tableReference + " AS " + tr.getAlias();
        return tableReference;
    }

    @Override
    public String visit(ComparisonPredicate cp) {
        String operation = switch (cp.getOperator()) {
            case EQ -> " = ";
            case NEQ -> " <> ";
            case LT -> " < ";
            case LEQ -> " <= ";
            case GT -> " > ";
            case GEQ -> " >= ";
        };
        return cp.getLeftExpression().visit(this) + operation + cp.getRightExpression().visit(this);
    }

    @Override
    public String visit(ValueListInPredicate vlip) {
        List<ValueExpression> valueList = vlip.getValueList();
        String valuesListString = valueList.stream()
            .map(e -> e.visit(this))
            .collect(Collectors.joining(", "));
        return vlip.getMainExpression().visit(this) + " IN ( " + valuesListString + " )";
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
            case OR -> " OR ";
        };
        return po.getLeftExpression().visit(this) + operation + po.getRightExpression().visit(this);
    }

    @Override
    public String visit(Assertion a) {
        String assertionName = (a.getSchemaReference() != null) ? a.getSchemaReference().visit(this) + "." : "";
        assertionName += a.getAssertionName();

        return "CREATE ASSERTION " + assertionName + " CHECK ( " + a.getBooleanExpression().visit(this) + " );";
    }

    @Override
    public String visit(View v) {
        if (v.getQuery().getAlias() != null) throw new IMPSqlException("Query of View cannot have an alias in TSQL.");

        String viewName = (v.getSchemaReference() != null) ? v.getSchemaReference().visit(this) + "." : "";
        viewName += v.getViewName();

        String viewCreationStatement = "CREATE VIEW " + viewName;
        if (v.getColumnNames() != null && !v.getColumnNames().isEmpty())
            viewCreationStatement += " ( " + String.join(", ", v.getColumnNames()) + " )";
        viewCreationStatement += " AS " + v.getQuery().visit(this) + ";";
        return viewCreationStatement;
    }

    @Override
    public String visit(NotOperation no) {
        return "NOT ( " + no.getExpression().visit(this) + " )";
    }

    @Override
    public String visit(ExistsPredicate ep) {
        if (ep.getQuery().getAlias() != null)
            throw new IMPSqlException("Query inside ExistsPredicate cannot have an alias in TSQL.");
        return "EXISTS " + ep.getQuery().visit(this);
    }

    @Override
    public String visit(SQLPrimitiveInteger i) {
        return String.valueOf(i.getValue());
    }

    @Override
    public String visit(SQLPrimitiveFloat f) {
        return String.valueOf(f.getValue());
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
        return asi.getExpression().visit(this) + " AS " + asi.getColumAlias();
    }

    @Override
    public String visit(SchemaReference sr) {
        StringBuilder fullNameBuilder = new StringBuilder();
        if (sr.getServerName() != null) fullNameBuilder.append(sr.getServerName()).append(".");
        if (sr.getDatabaseName() != null) fullNameBuilder.append(sr.getDatabaseName()).append(".");
        else if (sr.getServerName() != null) fullNameBuilder.append(".");
        fullNameBuilder.append(sr.getSchemaName());
        return fullNameBuilder.toString();
    }

    @Override
    public String visit(Table t) {
        String prefix = "";
        if (t.getSchemaReference() != null) prefix = t.getSchemaReference().visit(this)+".";
        List<TableConstraint> tableConstraints = t.getTableConstraints();
        String constraints = "";
        if (!tableConstraints.isEmpty()) constraints = ", " +
            String.join(", ", tableConstraints.stream().map(c -> c.visit(this)).toList());
        return "CREATE TABLE " + prefix + t.getTableName() + " ( "
            + String.join(", ", t.getAttributes().stream().map(a -> a.visit(this)).toList())
            + constraints + " );";
    }

    @Override
    public String visit(Attribute a) {
        return a.getName() + " " + a.getType().visit(this) +
            (a.hasDefaultExpression() ? " DEFAULT " + a.getDefaultExpression().visit(this) : "") +
            (a.isNotNull() ? " NOT NULL" : "");
    }

    @Override
    public String visit(Check c) {
        String checkCreationStatement = "";
        if (c.hasName()) checkCreationStatement = constraintBeginStatement(c.getName());
        checkCreationStatement += "CHECK (" +
            c.getExpression().visit(this) +
            ")";
        return checkCreationStatement;
    }

    @Override
    public String visit(Unique u) {
        String uniqueCreationStatement = "";
        if (u.hasName()) uniqueCreationStatement = constraintBeginStatement(u.getName());
        uniqueCreationStatement += "UNIQUE (" +
            String.join(", ", u.getAttributes().stream().map(Attribute::getName).toList())
            + ")";
        return uniqueCreationStatement;
    }

    @Override
    public String visit(PrimaryKey pk) {
        String pkCreationStatement = "";
        if (pk.hasName()) pkCreationStatement = constraintBeginStatement(pk.getName());
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
        if (fk.hasName()) fkCreationStatement = constraintBeginStatement(fk.getName());
        fkCreationStatement += "FOREIGN KEY (" +
            String.join(", ", fk.getFkAttributes().stream().map(Attribute::getName).toList());
        fkCreationStatement += ") REFERENCES " + prefix + fk.getPkReferenceTable().getTableName() + " ("+
            String.join(", ", fk.getPkReference().stream().map(Attribute::getName).toList())
            + ")";
        return fkCreationStatement;
    }

    private static String constraintBeginStatement(String name) {
        return "CONSTRAINT " + name + " ";
    }

    @Override
    public String visit(SQLCharacter c) {
        if (c.getLength() != null) return "CHAR(" + c.getLength() + ")";
        return "CHAR";
    }

    @Override
    public String visit(SQLVarchar v) {
        return "VARCHAR(" + v.getLength() + ")";
    }

    @Override
    public String visit(SQLBit b) {
        if (b.getLength() != null) return "BINARY(" + b.getLength() + ")";
        return "BINARY";
    }

    @Override
    public String visit(SQLInteger i) {
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
        return "DATE";
    }

    @Override
    public String visit(SQLTime t) {
        return "TIME(" + t.getFractionalSecondsPrecision() + ")";
    }

    @Override
    public String visit(SQLTimestamp ts) {
        return "TIMESTAMP";
    }

    @Override
    public String visit(SQLDoublePrecision dp) {
        return "DOUBLE PRECISION";
    }

    @Override
    public String visit(SQLNumeric n) {
        return "NUMERIC(" + n.getPrecision() + "," + n.getScale() + ")";
    }

    @Override
    public String visit(SQLDecimal d) {
        return "DECIMAL(" + d.getPrecision() + "," + d.getScale() + ")";
    }

    @Override
    public String visit(SQLDateTime dt) {
        return "DATETIME(" + dt.getFractionalSecondsPrecision() + ")";
    }

    @Override
    public String visit(SQLFunction f) {
        return f.getFunctionName() + "(" + String.join(", ", f.getArguments().stream().map(p -> p.visit(this)).toList()) + ")";
    }

    @Override
    public String visit(SQLVarbit vb) {
        if (vb.getLength() != null) return "VARBINARY(" + vb.getLength() + ")";
        return "VARBINARY";
    }
}
