package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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
        this(selectClause, fromClause, whereClause, null, false);
    }

    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, boolean isFirstLevel) {
        this(selectClause, fromClause, whereClause, null, isFirstLevel);
    }

    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, String alias) {
        this(selectClause, fromClause, whereClause, alias, false);
    }

    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, String alias, boolean isFirstLevel) {
        super(alias, isFirstLevel);
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
    public Query getFirstLevelCopy() {
        return new TableExpression(selectClause, fromClause, whereClause, true);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof TableExpression te
            && Objects.equals(getAlias(), te.getAlias())
            && selectClause.equals(te.selectClause)
            && Objects.equals(fromClause, te.fromClause)
            && Objects.equals(whereClause, te.whereClause);
    }
}
