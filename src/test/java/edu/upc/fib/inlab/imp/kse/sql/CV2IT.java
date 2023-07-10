package edu.upc.fib.inlab.imp.kse.sql;

import edu.upc.fib.inlab.imp.kse.sql.services.fetcher.SQLObjectSchemaFetcher;
import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.services.printer.SQLServerPrinter;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;
import edu.upc.fib.inlab.imp.kse.sql.utils.TintinAssertionsProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Objects.isNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class CV2IT {

    private static String serverName;
    private SQLObjectSchemaFetcher cv2_fetcher;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST"))? "localhost": System.getenv("SQL_DB_HOST");
    }

    @BeforeEach
    public void fetcherSetUp() {
        cv2_fetcher = new SQLObjectSchemaFetcher(
            serverName,
            1433,
                "SA", "PasswordO1.", "cv2_db",
            List.of("user_schema"),
                SQLObjectSchemaFetcher.DBType.SQLServer
        );
    }

    @Test
    public void fetchingCV2Schema() {
        cv2_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = cv2_fetcher.getSQLObjectSchema();

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());
    }

   @Test
    public void parsingCV2AssertionsWithFetchedTables() {
        cv2_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = cv2_fetcher.getSQLObjectSchema();

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser.parse(TintinAssertionsProvider.getCV2Assertions(),
            new SchemaReference("cv2_db","user_schema"));
        SQLObjectSchema cv2SchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !cv2SchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    public void cv2Assertions() {
        cv2_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = cv2_fetcher.getSQLObjectSchema();

        SQLObjectSchemaParser parser1 = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser1.parse(TintinAssertionsProvider.getCV2Assertions(),
            new SchemaReference("cv2_db","user_schema"));
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", schema1.getAssertions().stream().map(a->a.<String>visit(printer)).toList());

        SQLObjectSchemaParser parser2 = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser2.parse(printedAssertions,
            new SchemaReference("cv2_db","user_schema"));
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions.equals(schema2.getAssertions()));
    }
}
