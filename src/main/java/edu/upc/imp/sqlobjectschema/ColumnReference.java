package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class ColumnReference implements ValueExpression {
    private final FullTableName tableName;
    private final String columnName;

    public ColumnReference(FullTableName tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName.getTableName();
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnReference c
            && (tableName == null ? c.tableName == null : tableName.equals(c.tableName)
            && columnName.equals(c.columnName));
    }
}
