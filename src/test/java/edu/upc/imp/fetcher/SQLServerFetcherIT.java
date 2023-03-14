package edu.upc.imp.fetcher;

import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SQLServerFetcherIT {

    @Test
    public void fetchCV2Tables() {
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "test_db",
            List.of("test_schema", "ref_test_schema"),
            "SA",
            "PasswordO1.",
            SQLObjectSchemaFetcher.DBType.SQLServer
        );

        fetcher.fetch();

        SQLObjectSchema schema = fetcher.getSQLObjectSchema();
        SQLServerPrinter printer = new SQLServerPrinter();

        for (Table t : schema.getTables()) System.out.println(t.<String>visit(printer));
    }
}
