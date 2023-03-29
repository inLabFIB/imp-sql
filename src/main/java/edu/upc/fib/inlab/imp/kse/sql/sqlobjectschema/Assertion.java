package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Constraint;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Assertion implements Constraint {

    private final String assertionName;
    private final SchemaReference schemaReference;
    private final BooleanExpression booleanExpression;

    public Assertion(String assertionName, SchemaReference schemaReference, BooleanExpression booleanExpression) {
        this.assertionName = Objects.requireNonNull(assertionName, "The parameter 'assertionName' cannot be null.");
        this.schemaReference = schemaReference;
        this.booleanExpression = Objects.requireNonNull(booleanExpression, "The parameter 'booleanExpression' cannot be null.");
    }

    public Assertion(String assertionName, BooleanExpression booleanExpression) {
        this(assertionName, null, booleanExpression);
    }

    public String getAssertionName() {
        return assertionName;
    }

    public SchemaReference getSchemaReference() {
        return schemaReference;
    }

    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Assertion a
            && assertionName.equals(a.assertionName)
            && Objects.equals(schemaReference, a.schemaReference)
            && booleanExpression.equals(a.booleanExpression);
    }
}
