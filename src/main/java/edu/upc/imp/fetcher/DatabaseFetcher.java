package edu.upc.imp.fetcher;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;

import java.util.List;

public interface DatabaseFetcher {
    void fetch(String dbName, List<String> schemaNames, SQLObjectSchema schema);
}
