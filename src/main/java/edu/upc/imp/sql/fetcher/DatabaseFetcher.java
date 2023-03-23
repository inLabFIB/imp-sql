package edu.upc.imp.sql.fetcher;

import edu.upc.imp.sql.sqlobjectschema.SQLObjectSchema;

import java.util.List;

public interface DatabaseFetcher {
    void fetch(String dbName, List<String> schemaNames, SQLObjectSchema schema);
}
