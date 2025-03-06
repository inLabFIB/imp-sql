package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.TableSource;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableReference extends AliasableRelationalExpression {

    private final TableSource tableSource;

    public TableReference(TableSource tableSource, String alias) {
        super(alias);
        this.tableSource = Objects.requireNonNull(tableSource, "A table reference must be linked with a TableSource object previously defined in the IMP-SQL instance.");
    }

    public TableReference(TableSource tableSource) {
        this(tableSource, null);
    }

    public TableSource getTableSource() {
        return tableSource;
    }

    @Override
    public List<ColumnReference> getOfferedReferences() {
        List<ColumnReference> result = new ArrayList<>();

        String tableAlias = getAlias();
        if (tableAlias == null) tableAlias = tableSource.getName(); // Default alias

        // Assuming column references' table aliases do not contain schema reference information
        for (String columnName : tableSource.getColumnNames()) {
            result.add(new ColumnReference(tableAlias, columnName));
        }

        return result;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableReference(tableSource, newAlias);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableReference that = (TableReference) o;

        if (getAlias() != null ? !getAlias().equals(that.getAlias()) : that.getAlias() != null) return false;
        return tableSource.equals(that.tableSource);
    }

    @Override
    public int hashCode() {
        int result = getAlias() != null ? getAlias().hashCode() : 0;
        result = 31 * result + tableSource.hashCode();
        return result;
    }
}
