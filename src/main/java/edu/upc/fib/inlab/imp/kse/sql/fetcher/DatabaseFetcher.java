package edu.upc.fib.inlab.imp.kse.sql.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;

import java.util.List;

public interface DatabaseFetcher {
    void fetch(String dbName, List<String> schemaNames, SQLObjectSchema schema);
}
