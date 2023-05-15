package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.NotOperation;
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

    public View getEquivalentViolationDetectionView() {
        if (!(booleanExpression instanceof NotOperation nop && nop.getExpression() instanceof ExistsPredicate ep))
            throw new RuntimeException("Only assertions with predicate of type \"NOT EXISTS (<query>)\" have equivalent violation detection views.");
        SchemaReference newSchemaReference = (getSchemaReference() != null) ? new SchemaReference(getSchemaReference().getSchemaName()) : null;
        return new View(getAssertionName(), newSchemaReference, ep.getQuery());
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Assertion assertion = (Assertion) o;

        if (!assertionName.equals(assertion.assertionName)) return false;
        if (!Objects.equals(schemaReference, assertion.schemaReference))
            return false;
        return booleanExpression.equals(assertion.booleanExpression);
    }

    @Override
    public int hashCode() {
        int result = assertionName.hashCode();
        result = 31 * result + (schemaReference != null ? schemaReference.hashCode() : 0);
        result = 31 * result + booleanExpression.hashCode();
        return result;
    }
}
