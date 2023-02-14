package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.List;

public class TableExpression extends Query implements SQLObjectSchemaEntity {
    // Invariable rule: columnAliases.length = selectClause.length && tableAliases.length = fromClause.length

    // Aliases can be seen by other classes by using getters
    private List<String> columnAliases; // Aliases visible to outer queries (defined in SELECT clause).
    private List<String> tableAliases;  // Aliases visible to sub-queries (defined in FROM clause).

    // Private structures, an element of which can be retrieved by searching for their alias, or by index.
    // The relationship between aliases and fromClause/selectClause elements is positional:
    //     e.g. columnAliases[x] -> selectClause[x]
    private List<ValueExpression> selectClause;
    private List<RelationalExpression> fromClause;

    private BooleanExpression whereClause;

    @Override
    public String visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
