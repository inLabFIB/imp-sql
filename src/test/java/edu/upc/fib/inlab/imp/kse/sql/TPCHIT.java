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
import static org.hamcrest.Matchers.equalTo;

public class TPCHIT {

    private static String serverName;
    private SQLObjectSchemaFetcher tpch_fetcher;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST"))? "localhost": System.getenv("SQL_DB_HOST");
    }

    @BeforeEach
    public void fetcherSetUp() {
        tpch_fetcher = new SQLObjectSchemaFetcher(
            serverName,
            1433,
                "SA", "PasswordO1.", "tpch_db",
            List.of("user_schema"),
                SQLObjectSchemaFetcher.DBType.SQLServer
        );
    }

    @Test
    public void fetchingTPCHSchema() {
        tpch_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = tpch_fetcher.getSQLObjectSchema();

        assertThat("Fetcher didn't correctly fetch the TPCH schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());
    }

   @Test
    public void parsingTPCHAssertionsWithFetchedTables() {
        tpch_fetcher.fetch();
        SQLObjectSchema tpchFetchedSchema = tpch_fetcher.getSQLObjectSchema();

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(tpchFetchedSchema);
        parser.parse(TintinAssertionsProvider.getTPCHAssertions(), new SchemaReference("tpch_db", "user_schema"));
        SQLObjectSchema tpchSchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !tpchSchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    public void TPCHAssertions() {
        tpch_fetcher.fetch();
        SQLObjectSchema tpchFetchedSchema = tpch_fetcher.getSQLObjectSchema();

        SQLObjectSchemaParser parser1 = new SQLObjectSchemaParser(tpchFetchedSchema);
        parser1.parse(TintinAssertionsProvider.getTPCHAssertions(), new SchemaReference("tpch_db", "user_schema"));
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", expectedAssertions.stream().map(a->a.<String>visit(printer)).toList());

        SQLObjectSchemaParser parser2 = new SQLObjectSchemaParser(tpchFetchedSchema);
        parser2.parse(printedAssertions);
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        List<Assertion> printedAndParsedAssertions = schema2.getAssertions();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions, equalTo(printedAndParsedAssertions));
    }
}
