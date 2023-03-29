package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * String convention: all the names stored must not contain beginning and ending brackets or commas or any kind of
 * limiting character. This will be added in the printer if called.
 */
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
            && ((serverName == null && ftn.serverName == null) || (serverName != null && serverName.equalsIgnoreCase(ftn.serverName)))
            && ((databaseName == null && ftn.databaseName == null) || (databaseName != null && databaseName.equalsIgnoreCase(ftn.databaseName)))
            && schemaName.equalsIgnoreCase(ftn.schemaName);
    }

}
