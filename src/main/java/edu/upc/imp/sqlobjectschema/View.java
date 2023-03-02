package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    public View(String viewName, Query query) {
        this(viewName, null, null, query);
    }


    //TODO: add other constructors to avoid not nulls if needed
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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof View v
            && viewName.equals(v.viewName)
            && Objects.equals(schemaReference, v.schemaReference)
            && Objects.equals(columnNames, v.columnNames)
            && query.equals(v.query);
    }
}
