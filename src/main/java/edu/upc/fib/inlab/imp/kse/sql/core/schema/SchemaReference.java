package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.Objects;

/**
 * String convention: all the names stored must not contain beginning and ending brackets or commas or any kind of
 * limiting character. This will be added in the printer if called.
 */
public class SchemaReference implements SQLObjectSchemaValueObject {

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
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SchemaReference that = (SchemaReference) o;

        if (!Objects.equals(serverName, that.serverName)) return false;
        if (!Objects.equals(databaseName, that.databaseName)) return false;
        return schemaName.equals(that.schemaName);
    }

    @Override
    public int hashCode() {
        int result = serverName != null ? serverName.hashCode() : 0;
        result = 31 * result + (databaseName != null ? databaseName.hashCode() : 0);
        result = 31 * result + schemaName.hashCode();
        return result;
    }
}
