package edu.upc.imp.fetcher;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;

public interface DatabaseFetcher {
    void fetch(String dbName, String schemaName, SQLObjectSchema schema);
}
