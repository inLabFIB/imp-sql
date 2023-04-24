package edu.upc.fib.inlab.imp.kse.sql.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.services.fetcher.sql_server.SQLServerFetcher;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;

import javax.sql.DataSource;
import java.util.List;

public class SQLObjectSchemaFetcher {

    private final String dbName;
    private final List<String> schemaNames;

    private final DatabaseFetcher dbFetcher;

    private final SQLObjectSchema schema;

    public enum DBType {
        SQLServer
    }

    public SQLObjectSchemaFetcher(String serverName, int port, String user, String pwd, String dbName, List<String> schemaNames, DBType dbType) {
        if (serverName == null || dbName == null || schemaNames == null || user == null || pwd == null || dbType == null)
            throw new IllegalArgumentException("All necessary parameters must be specified.");

        switch (dbType) {
            case SQLServer -> dbFetcher = new SQLServerFetcher(serverName, port, dbName, user, pwd);
            default -> throw new IllegalArgumentException("Database type '" + dbType + "' not supported.");
        }

        this.dbName = dbName;
        this.schemaNames = schemaNames;
        this.schema = new SQLObjectSchema();
    }

    public SQLObjectSchemaFetcher (DataSource dataSource, String dbName, List<String> schemaNames, DBType dbType) {
        if (dataSource == null || dbName == null || schemaNames == null || dbType == null)
            throw new IllegalArgumentException("All necessary parameters must be specified.");

        switch (dbType) {
            case SQLServer -> dbFetcher = new SQLServerFetcher(dataSource);
            default -> throw new IllegalArgumentException("Database type '" + dbType + "' not supported.");
        }

        this.dbName = dbName;
        this.schemaNames = schemaNames;
        this.schema = new SQLObjectSchema();
    }

    public void fetch() {
        dbFetcher.fetch(dbName, schemaNames, schema);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }
}
