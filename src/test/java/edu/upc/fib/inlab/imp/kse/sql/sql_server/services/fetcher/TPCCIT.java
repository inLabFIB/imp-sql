package edu.upc.fib.inlab.imp.kse.sql.sql_server.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import edu.upc.fib.inlab.imp.kse.sql.core.utils.TintinAssertionsProvider;
import edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer.SQLServerPrinter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Objects.isNull;
import static org.hamcrest.MatcherAssert.assertThat;

class TPCCIT {

    static final String TPCC_DB = "tpcc_db";
    private static String serverName;
    private SQLServerFetcher tpcc_fetcher;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST")) ? "localhost" : System.getenv("SQL_DB_HOST");
    }

    @BeforeEach
    void fetcherSetUp() {
        tpcc_fetcher = new SQLServerFetcher(serverName, 1433, TPCC_DB, "SA", "PasswordO1.");
    }

    @Test
    void fetchingTPCCSchema() {
        SQLObjectSchema cv2FetchedSchema = tpcc_fetcher.fetch(TPCC_DB, List.of("user_schema"));

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());
    }

    @Test
    void parsingTPCCAssertionsWithFetchedTables() {
        SQLObjectSchema cv2FetchedSchema = tpcc_fetcher.fetch(TPCC_DB, List.of("user_schema"));

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());

        StandardSQLParser parser = new StandardSQLParser(cv2FetchedSchema);
        parser.parse(TintinAssertionsProvider.getTPCCAssertions(),
            new SchemaReference(TPCC_DB, "user_schema"));
        SQLObjectSchema cv2SchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !cv2SchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    void TPCCAssertions() {
        SQLObjectSchema cv2FetchedSchema = tpcc_fetcher.fetch(TPCC_DB, List.of("user_schema"));

        StandardSQLParser parser1 = new StandardSQLParser(cv2FetchedSchema);
        parser1.parse(TintinAssertionsProvider.getTPCCAssertions(),
            new SchemaReference(TPCC_DB, "user_schema"));
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", schema1.getAssertions().stream().map(a -> a.<String>visit(printer)).toList());

        StandardSQLParser parser2 = new StandardSQLParser(cv2FetchedSchema);
        parser2.parse(printedAssertions,
            new SchemaReference(TPCC_DB, "user_schema"));
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions.equals(schema2.getAssertions()));
    }
}
