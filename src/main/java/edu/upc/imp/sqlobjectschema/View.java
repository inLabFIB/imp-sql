package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.List;

public class View implements SQLObjectSchemaEntity {

    private final FullTableName viewName;
    private final List<String> columnNames;
    private final Query query;

    public View(FullTableName viewName, List<String> columnNames, Query query) {
        this.viewName = viewName;
        this.columnNames = columnNames;
        this.query = query;
    }

    public View(FullTableName viewName, Query query) {
        this(viewName, null, query);
    }

    public FullTableName getViewName() {
        return viewName;
    }

    public List<String> getColumnNames() {
        return columnNames;
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
            && (viewName.equals(v.viewName)
            && columnNames == null ? v.columnNames == null : columnNames.equals(v.columnNames)
            && query == v.query);
    }
}
