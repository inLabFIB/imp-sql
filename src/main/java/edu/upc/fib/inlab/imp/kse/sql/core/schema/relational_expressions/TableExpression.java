package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class TableExpression extends Query {

    private final List<SelectItem> selectClause;
    private final RelationalExpression fromClause;
    private final BooleanExpression whereClause;

    //CACHE for optimized translation, might delete in the future
    private final List<AliasableRelationalExpression> fromClauseTerminalExpressions;

    public TableExpression(List<SelectItem> selectClause) {
        this(selectClause, null, null, null);
    }

    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause) {
        this(selectClause, fromClause, whereClause, null);
    }


    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, String alias) {
        super(alias);
        this.selectClause = Objects.requireNonNull(selectClause, "The select clause of a TableExpression cannot be null.");
        this.fromClause = fromClause;
        this.whereClause = whereClause;

        if (this.selectClause.isEmpty()) throw new IMPSqlException("SELECT clause cannot be empty.");
        if (this.selectClause.stream().anyMatch(Asterisk.class::isInstance) && this.fromClause == null)
            throw new IMPSqlException("Cannot select * without a FROM clause");

        this.fromClauseTerminalExpressions = getFromClauseTerminalExpressions(fromClause);
    }

    private static List<AliasableRelationalExpression> getFromClauseTerminalExpressions(RelationalExpression fromClause) {
        List<AliasableRelationalExpression> tempFromClauseTerminalExpressions = new ArrayList<>();
        // Traverse fromClause joins to find aliases and terminal expressions
        if (fromClause != null) {
            Stack<RelationalExpression> toVisit = new Stack<>();
            toVisit.push(fromClause);
            while (!toVisit.empty()) {
                RelationalExpression next = toVisit.pop();
                if (next instanceof JoinOperation join) {
                    toVisit.push(join.getLeftExpression());
                    toVisit.push(join.getRightExpression());
                } else if (next instanceof AliasableRelationalExpression terminal) {
                    tempFromClauseTerminalExpressions.add(terminal);
                }
            }
        }
        return tempFromClauseTerminalExpressions;
    }

    public List<SelectItem> getSelectClause() {
        return new ArrayList<>(selectClause);
    }
    public RelationalExpression getFromClause() {
        return fromClause;
    }
    public BooleanExpression getWhereClause() {
        return whereClause;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableExpression(selectClause, fromClause, whereClause, newAlias);
    }

    public List<AliasableRelationalExpression> getFromClauseTerminalExpressions() {
        return new ArrayList<>(fromClauseTerminalExpressions);
    }

    @Override
    public List<ColumnReference> getOfferedReferences() {
        List<ColumnReference> result = new ArrayList<>();

        String superAlias = getAlias();
        for (SelectItem s : getSelectClause()) {
            if (s instanceof Asterisk && fromClause != null) {
                fromClause.getOfferedReferences()
                    .forEach(r -> result.add(new ColumnReference(superAlias, r.getColumnName())));
            } else if (s instanceof AliasableSelectItem as){
                String selectAlias = as.getColumAlias();
                if (selectAlias == null) selectAlias = as.getDefaultAlias();
                if (selectAlias == null)
                    throw new IMPSqlException("No column alias specified for '" + superAlias + "'");
                result.add(new ColumnReference(superAlias, selectAlias));
            }
        }

        return result;
    }

    @Override
    public String computeDefaultColumnAlias() {
        return getAlias();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TableExpression that = (TableExpression) o;

        if (!selectClause.equals(that.selectClause)) return false;
        if (!Objects.equals(fromClause, that.fromClause)) return false;
        if (!Objects.equals(whereClause, that.whereClause)) return false;
        return Objects.equals(fromClauseTerminalExpressions, that.fromClauseTerminalExpressions);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + selectClause.hashCode();
        result = 31 * result + (fromClause != null ? fromClause.hashCode() : 0);
        result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
        result = 31 * result + (fromClauseTerminalExpressions != null ? fromClauseTerminalExpressions.hashCode() : 0);
        return result;
    }
}
