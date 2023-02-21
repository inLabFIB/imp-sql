package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

public class FullTableName implements SQLObjectSchemaEntity {

    private final String serverName;
    private final String databaseName;
    private final String schemaName;
    private final String tableName;

    public FullTableName(String tableName) {
        this(null, null, null, tableName);
    }

    public FullTableName(String schemaName, String tableName) {
        this(null, null, schemaName, tableName);
    }

    public FullTableName(String databaseName, String schemaName, String tableName) {
        this(null, databaseName, schemaName, tableName);
    }

    public FullTableName(String serverName, String databaseName, String schemaName, String tableName) {
        this.serverName = serverName;
        this.databaseName = databaseName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getServerName() {
        return serverName;
    }

    /**
     * Might not be standard
     */
    public String getFullTableName() {
        // TODO: Check specification, if it is possible to do serverName...tableName, or similar
        if (tableName == null) throw new RuntimeException("No table name specified.");
        String fullName = "";
        if (serverName != null) fullName += serverName + ".";
        if (databaseName != null) fullName += databaseName + ".";
        if (schemaName != null) fullName += schemaName + ".";
        fullName += tableName;
        return fullName;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof FullTableName ftn
            && (serverName == null ? ftn.serverName == null : serverName.equals(ftn.serverName)
            && databaseName == null ? ftn.databaseName == null : databaseName.equals(ftn.databaseName)
            && schemaName == null ? ftn.schemaName == null : schemaName.equals(ftn.schemaName)
            && tableName.equals(ftn.tableName));
    }
}
