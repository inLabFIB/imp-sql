package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

public class SchemaReference implements SQLObjectSchemaEntity {

    private final String serverName;
    private final String databaseName;
    private final String schemaName;

    public SchemaReference(String schemaName) {
        this(null, null, schemaName);
    }

    public SchemaReference(String databaseName, String schemaName) {
        this(null, databaseName, schemaName);
    }

    public SchemaReference(String serverName, String databaseName, String schemaName) {
        this.serverName = serverName;
        this.databaseName = databaseName;
        this.schemaName = Objects.requireNonNull(schemaName, "The parameter 'schemaName' cannot be null.");
    }

    public String getServerName() {
        return serverName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SchemaReference ftn
            && Objects.equals(serverName, ftn.serverName)
            && Objects.equals(databaseName, ftn.databaseName)
            && schemaName.equals(ftn.schemaName);
    }

}
