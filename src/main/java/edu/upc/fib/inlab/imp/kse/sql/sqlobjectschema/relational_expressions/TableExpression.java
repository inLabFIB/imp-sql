package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause) {
        this(selectClause, fromClause, whereClause, null);
    }


    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, String alias) {
        super(alias);
        this.selectClause = Objects.requireNonNull(selectClause, "The select clause of a TableExpression cannot be null.");
        this.fromClause = fromClause;
        this.whereClause = whereClause;

        List<AliasableRelationalExpression> tempFromClauseTerminalExpressions = new ArrayList<>();
        // Traverse fromClause joins to find aliases and terminal expressions
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
        this.fromClauseTerminalExpressions = tempFromClauseTerminalExpressions;
    }

    public int getNumberOfSelectClauseItems() {
        return selectClause.size();
    }
    public String getNthSelectionAlias(int n) {
        return selectClause.get(n).getColumAlias();
    }
    public SelectItem getNthSelectionValue(int n) {
        return selectClause.get(n);
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
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableExpression(selectClause, fromClause, whereClause, newAlias);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableExpression that = (TableExpression) o;

        if (getAlias() != null ? !getAlias().equals(that.getAlias()) : that.getAlias() != null) return false;
        if (!selectClause.equals(that.selectClause)) return false;
        if (!Objects.equals(fromClause, that.fromClause)) return false;
        return Objects.equals(whereClause, that.whereClause);
    }

    @Override
    public int hashCode() {
        int result = getAlias() != null ? getAlias().hashCode() : 0;
        result = 31 * result + selectClause.hashCode();
        result = 31 * result + (fromClause != null ? fromClause.hashCode() : 0);
        result = 31 * result + (whereClause != null ? whereClause.hashCode() : 0);
        return result;
    }
}
