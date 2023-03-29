package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class TableReference extends AliasableRelationalExpression {

    private final Table table;

    public TableReference(Table table, String alias) {
        super(alias);
        this.table = Objects.requireNonNull(table, "A table reference must be linked with a Table object previouslly defined in the IMP-SQL instance.");
    }

    public TableReference(Table table) {
        this(table, null);
    }

    public Table getTable() {
        return table;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new TableReference(table, newAlias);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof TableReference t
            && Objects.equals(getAlias(), t.getAlias())
            && table.equals(t.table);
    }
}
