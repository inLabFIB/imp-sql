package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * For now it only considers AS statements. Equality alias can be added in the future.
 */
public class AliasableSelectItem implements SelectItem {

    private final String columnAlias;
    private final ValueExpression expression;

    public AliasableSelectItem(String columnAlias, ValueExpression expression) {
        this.columnAlias = columnAlias;
        this.expression = expression;
    }

    public ValueExpression getExpression() {
        return expression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public String getColumAlias() {
        return columnAlias;
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof AliasableSelectItem asi
            && Objects.equals(columnAlias, asi.columnAlias)
            && expression.equals(asi.expression);
    }
}
