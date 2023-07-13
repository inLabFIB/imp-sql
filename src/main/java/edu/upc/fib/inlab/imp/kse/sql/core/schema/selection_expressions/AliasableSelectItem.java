package edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ValueExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * For now, it only considers AS statements. Equality alias can be added in the future.
 */
public class AliasableSelectItem implements SelectItem {

    private final String columnAlias;
    private final ValueExpression expression;

    public AliasableSelectItem(ValueExpression expression, String columnAlias) {
        this.expression = Objects.requireNonNull(expression, "The parameter 'expression' cannot be null.");
        this.columnAlias = columnAlias;
    }

    public AliasableSelectItem(ValueExpression expression) {
        this(expression, null);
    }

    public ValueExpression getExpression() {
        return expression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    public String getColumAlias() {
        return columnAlias;
    }

    public String getDefaultAlias() {
        return this.expression.computeDefaultColumnAlias();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AliasableSelectItem that = (AliasableSelectItem) o;

        if (!Objects.equals(columnAlias, that.columnAlias)) return false;
        return expression.equals(that.expression);
    }

    @Override
    public int hashCode() {
        int result = columnAlias != null ? columnAlias.hashCode() : 0;
        result = 31 * result + expression.hashCode();
        return result;
    }
}
