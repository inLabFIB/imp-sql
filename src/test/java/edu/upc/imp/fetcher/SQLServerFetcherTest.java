package edu.upc.imp.fetcher;

import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import org.junit.jupiter.api.Test;

public class SQLServerFetcherTest {
    @Test
    public void fetchCV2Tables() {
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "cv2_db",
            "user_schema",
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
