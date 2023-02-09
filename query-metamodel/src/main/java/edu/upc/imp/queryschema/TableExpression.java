package edu.upc.imp.queryschema;

import edu.upc.imp.queryschema.visitor.QuerySchemaObject;
import edu.upc.imp.queryschema.visitor.QuerySchemaVisitor;

import java.util.List;

public class TableExpression extends Query implements QuerySchemaObject {

    private List<String> columnAliases;
    private List<String> tableAliases;

    private List<RelationalExpression> fromClause;
    private List<ValueExpression> selectClause;
    private BooleanExpression whereClause;

    @Override
    public void visit(QuerySchemaVisitor visitor) {
        visitor.visit(this);
    }
}
