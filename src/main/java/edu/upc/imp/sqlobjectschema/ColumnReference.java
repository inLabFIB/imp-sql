package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class ColumnReference implements ValueExpression {
    private final String tableAlias;
    private final String columnAlias;

    public ColumnReference(String tableAlias, String columnAlias) {
        this.tableAlias = tableAlias;
        this.columnAlias = columnAlias;
    }

    public String getTableAlias() {
        return tableAlias;
    }

    public String getColumnAlias() {
        return columnAlias;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
