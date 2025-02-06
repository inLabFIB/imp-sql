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

class CV2IT {

    static final String CV2_DB = "cv2_db";
    private static String serverName;

    private SQLServerFetcher cv2_fetcher;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST"))? "localhost": System.getenv("SQL_DB_HOST");
    }

    @BeforeEach
    void fetcherSetUp() {
        cv2_fetcher = new SQLServerFetcher(serverName, 1433, CV2_DB,"SA", "PasswordO1.");
    }

    @Test
    void fetchingCV2Schema() {
        SQLObjectSchema cv2FetchedSchema = cv2_fetcher.fetch(CV2_DB, List.of("user_schema"));

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());
    }

   @Test
   void parsingCV2AssertionsWithFetchedTables() {
       SQLObjectSchema cv2FetchedSchema = cv2_fetcher.fetch(CV2_DB, List.of("user_schema"));

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());

       StandardSQLParser parser = new StandardSQLParser(cv2FetchedSchema);
        parser.parse(TintinAssertionsProvider.getCV2Assertions(),
            new SchemaReference(CV2_DB,"user_schema"));
        SQLObjectSchema cv2SchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !cv2SchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    void cv2Assertions() {
        SQLObjectSchema cv2FetchedSchema = cv2_fetcher.fetch(CV2_DB, List.of("user_schema"));

        StandardSQLParser parser1 = new StandardSQLParser(cv2FetchedSchema);
        parser1.parse(TintinAssertionsProvider.getCV2Assertions(),
            new SchemaReference(CV2_DB,"user_schema"));
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", schema1.getAssertions().stream().map(a->a.<String>visit(printer)).toList());

        StandardSQLParser parser2 = new StandardSQLParser(cv2FetchedSchema);
        parser2.parse(printedAssertions,
            new SchemaReference(CV2_DB,"user_schema"));
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions.equals(schema2.getAssertions()));
    }
}
