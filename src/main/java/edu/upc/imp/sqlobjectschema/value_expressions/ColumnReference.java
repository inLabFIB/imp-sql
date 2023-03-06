package edu.upc.imp.sqlobjectschema.value_expressions;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class ColumnReference implements ValueExpression {

    private final String tableName;

    private final String columnName;
    public ColumnReference(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = Objects.requireNonNull(columnName, "The parameter 'columnName' cannot be null.");
    }

    public ColumnReference(String columnName) {
        this(null, columnName);
    }



    public String getTableName() {
        return tableName;
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
            && Objects.equals(tableName, c.tableName)
            && columnName.equals(c.columnName);
    }
}
