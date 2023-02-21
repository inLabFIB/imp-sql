package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class TableExpression extends Query {
    // Invariable rule: columnAliases.length = selectClause.length && tableAliases.length = fromClause.length

    // Aliases can be seen by other classes by using getters
    // The relationship between aliases and fromClause/selectClause elements is positional:
    //     e.g. columnAliases[x] -> selectClause[x]

    private final List<SelectItem> selectClause;

    private final RelationalExpression fromClause;
    private final BooleanExpression whereClause;
    //CACHE for optimized translation, might delete in the future

    private final List<AliasableRelationalExpression> fromClauseTerminalExpressions;
    public TableExpression(List<SelectItem> selectClause, RelationalExpression fromClause, BooleanExpression whereClause, String alias) {
        super(alias);
        this.selectClause = selectClause;
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
        return selectClause;
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
}
