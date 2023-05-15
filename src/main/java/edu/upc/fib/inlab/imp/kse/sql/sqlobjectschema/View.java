package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.Query;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class View implements SQLObjectSchemaEntity {

    private final String viewName;
    private final SchemaReference schemaReference;
    private final List<String> columnNames;
    private final Query query;

    public View(String viewName, SchemaReference schemaReference, List<String> columnNames, Query query) {
        this.viewName = Objects.requireNonNull(viewName, "The parameter 'viewName' cannot be null.");
        this.schemaReference = schemaReference;
        this.columnNames = columnNames;
        this.query = Objects.requireNonNull(query, "The parameter 'query' cannot be null.");
    }

    public View(String viewName, SchemaReference schemaReference, Query query) {
        this(viewName, schemaReference, null, query);
    }

    public View(String viewName, Query query) {
        this(viewName, null, null, query);
    }


    public String getViewName() {
        return viewName;
    }

    public SchemaReference getSchemaReference() {
        return schemaReference;
    }

    public List<String> getColumnNames() {
        return columnNames == null ? null : new ArrayList<>(columnNames);
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

        View view = (View) o;

        if (!viewName.equals(view.viewName)) return false;
        if (!Objects.equals(schemaReference, view.schemaReference))
            return false;
        if (!Objects.equals(columnNames, view.columnNames)) return false;
        return query.equals(view.query);
    }

    @Override
    public int hashCode() {
        int result = viewName.hashCode();
        result = 31 * result + (schemaReference != null ? schemaReference.hashCode() : 0);
        result = 31 * result + (columnNames != null ? columnNames.hashCode() : 0);
        result = 31 * result + query.hashCode();
        return result;
    }
}
