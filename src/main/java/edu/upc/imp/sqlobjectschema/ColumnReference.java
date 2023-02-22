package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class ColumnReference implements ValueExpression {

    private final FullTableName fullTableName;
    private final String columnName;

    public ColumnReference(FullTableName tableName, String columnName) {
        this.fullTableName = tableName;
        this.columnName = Objects.requireNonNull(columnName, "The parameter 'columnName' cannot be null.");
    }

    public ColumnReference(String columnName) {
        this(null, columnName);
    }

    public FullTableName getFullTableName() {
        return fullTableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getTableName() {
        return fullTableName.getTableName();
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof ColumnReference c
            && Objects.equals(fullTableName, c.fullTableName)
            && columnName.equals(c.columnName);
    }
}
