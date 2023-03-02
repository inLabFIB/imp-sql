package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class Table implements SQLObjectSchemaEntity {

    private final String tableName;
    private final SchemaReference schemaReference;


    public Table(String tableName, SchemaReference schemaReference) {
        this.tableName = Objects.requireNonNull(tableName, "The parameter 'tableName' cannot be null.");
        this.schemaReference = schemaReference;
    }

    public String getTableName() {
        return tableName;
    }

    public SchemaReference getSchemaReference() {
        return schemaReference;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    //TODO: implement this
    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return false;
    }

}
