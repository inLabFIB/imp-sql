package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableReference extends AliasableRelationalExpression {

    private final Table table;

    public TableReference(Table table, String alias) {
        super(alias);
        this.table = Objects.requireNonNull(table, "A table reference must be linked with a Table object previously defined in the IMP-SQL instance.");
    }

    public TableReference(Table table) {
        this(table, null);
    }

    public Table getTable() {
        return table;
    }

    public List<ColumnReference> getTableAliases() {
        List<ColumnReference> result = new ArrayList<>();

        String tableAlias = getAlias();
        if (tableAlias == null) tableAlias = table.getTableName(); // Default alias

        // Assuming column references' table aliases do not contain schema reference information
        for (Attribute a : table.getAttributes()) {
            result.add(new ColumnReference(tableAlias, a.getName()));
        }

        return result;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableReference(table, newAlias);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TableReference that = (TableReference) o;

        if (getAlias() != null ? !getAlias().equals(that.getAlias()) : that.getAlias() != null) return false;
        return table.equals(that.table);
    }

    @Override
    public int hashCode() {
        int result = getAlias() != null ? getAlias().hashCode() : 0;
        result = 31 * result + table.hashCode();
        return result;
    }
}
