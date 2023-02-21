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

    public String getViewName() {
        return viewName.getTableName();
    }

    public Query getQuery() {
        return query;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }
}
