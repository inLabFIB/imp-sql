package edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class ColumnReference implements ValueExpression {

    private final String tableName;
    private final String columnName;

    public ColumnReference(String columnName) {
        this(null, columnName);
    }

    public ColumnReference(String tableName, String columnName) {
        this.tableName = tableName;
        this.columnName = Objects.requireNonNull(columnName, "The parameter 'columnName' cannot be null.");
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public String computeDefaultColumnAlias() {
        return columnName;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int hashCode() {
        int result = tableName != null ? tableName.hashCode() : 0;
        result = 31 * result + columnName.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColumnReference that = (ColumnReference) o;

//        if (!Objects.equals(tableName, that.tableName)) return false;
        if(Objects.isNull(tableName) && !Objects.isNull(that.tableName)) return false;
        if(!Objects.isNull(tableName) && !tableName.equalsIgnoreCase(that.getTableName())) return false;
        return columnName.equalsIgnoreCase(that.columnName);
    }
}
