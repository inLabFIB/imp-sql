package edu.upc.imp.sqlobjectschema.constraints;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.value_expressions.ValueExpression;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Default extends TableConstraint {

    private final Attribute attribute;
    private final ValueExpression expression;

    public Default(String name, Attribute attribute, ValueExpression expression) {
        super(name);
        this.attribute = Objects.requireNonNull(attribute, "The parameter 'attribute' cannot be null.");
        this.expression = Objects.requireNonNull(expression, "The parameter 'expression' cannot be null.");
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public ValueExpression getExpression() {
        return expression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Default d
            && getName().equals(d.getName())
            && attribute.equals(d.attribute)
            && expression.equals(d.expression);
    }
}
