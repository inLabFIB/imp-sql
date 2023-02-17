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

    private final List<ValueExpression> selectClause;
    private final List<String> columnAliases; // Aliases visible to outer queries (defined in SELECT clause).
    private final RelationalExpression fromClause;

    private final List<RelationalExpression> fromClauseTerminalExpressions;
    private final List<String> tableAliases;  // Aliases visible to sub-queries (defined in FROM clause).

    private final BooleanExpression whereClause;

    public TableExpression(List<ValueExpression> selectClause, List<String> columnAliases, RelationalExpression fromClause, BooleanExpression whereClause, String alias) {
        super(alias);
        if (selectClause.size() != columnAliases.size()) throw new RuntimeException("selectClause and columnAliases must have the same size");
        this.selectClause = selectClause;
        this.columnAliases = columnAliases;
        this.fromClause = fromClause;
        this.whereClause = whereClause;

        List<RelationalExpression> tempFromClauseTerminalExpressions = new ArrayList<>();
        List<String> tempTableAliases = new ArrayList<>();
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
                tempTableAliases.add(terminal.getAlias());
            }
        }
        this.fromClauseTerminalExpressions = tempFromClauseTerminalExpressions;
        this.tableAliases = tempTableAliases;
    }

    public int getNumberOfSelectClauseItems() {
        return selectClause.size();
    }
    public String getNthSelectionAlias(int n) {
        return columnAliases.get(n);
    }
    public ValueExpression getNthSelectionValue(int n) {
        return selectClause.get(n);
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
}
