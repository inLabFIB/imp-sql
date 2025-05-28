package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class View implements TableSource {

    private final String viewName;
    private final SchemaReference schemaReference;
    private final List<String> explicitColumnNames;
    private final List<String> implicitColumnNames;
    private final Query query;

    public View(String viewName, SchemaReference schemaReference, List<String> explicitColumnNames, Query query) {
        this.viewName = Objects.requireNonNull(viewName, "The parameter 'viewName' cannot be null.");
        this.schemaReference = schemaReference;
        this.explicitColumnNames = explicitColumnNames;
        this.query = Objects.requireNonNull(query, "The parameter 'query' cannot be null.");
        List<String> queryColumNames = query.getOfferedReferences().stream()
            .map(ColumnReference::getColumnName)
            .toList();
        if (explicitColumnNames == null || explicitColumnNames.isEmpty()) {
            this.implicitColumnNames = queryColumNames;
        } else {
            if (explicitColumnNames.size() != queryColumNames.size())
                throw new IMPSqlException("Number of view explicit columns names does not match number of query column names");
            if (explicitColumnNames.size() != new HashSet<>(explicitColumnNames).size())
                throw new IMPSqlException("Repeated columns not allowed in view explicit column names");

            this.implicitColumnNames = explicitColumnNames;
        }
    }

    public View(String viewName, SchemaReference schemaReference, Query query) {
        this(viewName, schemaReference, null, query);
    }

    public View(String viewName, Query query) {
        this(viewName, null, null, query);
    }

    public String getName() {
        return getViewName();
    }

    public String getViewName() {
        return viewName;
    }

    public SchemaReference getSchemaReference() {
        return schemaReference;
    }

    public List<String> getColumnNames() {
        return explicitColumnNames == null ? implicitColumnNames : new ArrayList<>(explicitColumnNames);
    }

    public List<String> getExplicitColumnNames() {
        return explicitColumnNames == null ? List.of() : new ArrayList<>(explicitColumnNames);
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    public boolean hasSameIdentifier(String viewName, SchemaReference schemaReference) {
        return this.viewName.equalsIgnoreCase(viewName)
            && Objects.equals(this.schemaReference, schemaReference);
    }

    public boolean hasSameIdentifier(View v) {
        return viewName.equals(v.viewName)
            && Objects.equals(schemaReference, v.schemaReference);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        View view = (View) o;

        if (!viewName.equals(view.viewName)) return false;
        if (!Objects.equals(schemaReference, view.schemaReference))
            return false;
        if (!Objects.equals(explicitColumnNames, view.explicitColumnNames)) return false;
        return query.equals(view.query);
    }

    @Override
    public int hashCode() {
        int result = viewName.hashCode();
        result = 31 * result + (schemaReference != null ? schemaReference.hashCode() : 0);
        result = 31 * result + (explicitColumnNames != null ? explicitColumnNames.hashCode() : 0);
        result = 31 * result + query.hashCode();
        return result;
    }
}
