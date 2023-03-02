package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import javax.management.DescriptorAccess;
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
            && attribute.equals(d.attribute)
            && expression.equals(d.expression);
    }
}
