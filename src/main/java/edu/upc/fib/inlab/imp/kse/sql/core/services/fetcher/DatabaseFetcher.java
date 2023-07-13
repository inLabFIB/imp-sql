package edu.upc.fib.inlab.imp.kse.sql.core.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;

import java.util.List;

public interface DatabaseFetcher {
    SQLObjectSchema fetch(String dbName, List<String> schemaNames);
    void fetch(String dbName, List<String> schemaNames, SQLObjectSchema schema);
}
