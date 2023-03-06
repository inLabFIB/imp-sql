package edu.upc.imp.fetcher;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;

public class SQLObjectSchemaFetcher {

    private final String dbName;
    private final String schemaName;

    private final DatabaseFetcher dbFetcher;

    private final SQLObjectSchema schema;

    public enum DBType {
        SQLServer
    }

    public SQLObjectSchemaFetcher(String serverName, int port, String dbName, String schemaName, String user, String pwd, DBType dbType) {
        if (serverName == null || dbName == null || schemaName == null || user == null || pwd == null || dbType == null)
            throw new IllegalArgumentException("All necessary parameters must be specified.");

        switch (dbType) {
            case SQLServer -> dbFetcher = new SQLServerFetcher(serverName, port, dbName, user, pwd);
            default -> throw new IllegalArgumentException("Database type '" + dbType + "' not supported.");
        }

        this.dbName = dbName;
        this.schemaName = schemaName;
        this.schema = new SQLObjectSchema();
    }

    public void fetch() {
        dbFetcher.fetch(dbName, schemaName, schema);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }
}
