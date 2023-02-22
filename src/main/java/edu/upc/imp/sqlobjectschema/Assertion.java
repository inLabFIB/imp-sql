package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Assertion implements SQLObjectSchemaEntity {

    private final FullTableName assertionName;
    private final BooleanExpression booleanExpression;

    public Assertion(FullTableName name, BooleanExpression booleanExpression) {
        this.assertionName = Objects.requireNonNull(name, "The parameter 'name' cannot be null.");
        this.booleanExpression = Objects.requireNonNull(booleanExpression, "The parameter 'booleanExpression' cannot be null.");
    }

    public FullTableName getAssertionName() {
        return assertionName;
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
            && booleanExpression.equals(a.booleanExpression);
    }
}
