package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof ExistsPredicate ep
            && query.equals(ep.query);
    }
}
