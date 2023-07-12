package edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class ExistsPredicate extends Predicate {

    private final Query query;

    public ExistsPredicate(Query query) {
        this.query = Objects.requireNonNull(query, "The parameter 'query' cannot be null.");
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExistsPredicate that = (ExistsPredicate) o;

        return query.equals(that.query);
    }

    @Override
    public int hashCode() {
        return query.hashCode();
    }
}
