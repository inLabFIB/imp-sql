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
import static org.hamcrest.Matchers.equalTo;

class TPCHIT {

    static final String TPCH_DB = "tpch_db";
    private static String serverName;
    private SQLServerFetcher tpch_fetcher;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST"))? "localhost": System.getenv("SQL_DB_HOST");
    }

    @BeforeEach
    void fetcherSetUp() {
        tpch_fetcher = new SQLServerFetcher(serverName, 1433, TPCH_DB, "SA", "PasswordO1.");
    }

    @Test
    void fetchingTPCHSchema() {
        SQLObjectSchema tpchFetchedSchema = tpch_fetcher.fetch(TPCH_DB, List.of("user_schema"));

        assertThat("Fetcher didn't correctly fetch the TPCH schema tables.",
            !tpchFetchedSchema.getTables().isEmpty());
    }

   @Test
   void parsingTPCHAssertionsWithFetchedTables() {
        SQLObjectSchema tpchFetchedSchema = tpch_fetcher.fetch(TPCH_DB, List.of("user_schema"));

       StandardSQLParser parser = new StandardSQLParser(tpchFetchedSchema);
        parser.parse(TintinAssertionsProvider.getTPCHAssertions(), new SchemaReference(TPCH_DB, "user_schema"));
        SQLObjectSchema tpchSchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !tpchSchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    void TPCHAssertions() {
        SQLObjectSchema tpchFetchedSchema = tpch_fetcher.fetch(TPCH_DB, List.of("user_schema"));

        StandardSQLParser parser1 = new StandardSQLParser(tpchFetchedSchema);
        parser1.parse(TintinAssertionsProvider.getTPCHAssertions(), new SchemaReference(TPCH_DB, "user_schema"));
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", expectedAssertions.stream().map(a->a.<String>visit(printer)).toList());

        StandardSQLParser parser2 = new StandardSQLParser(tpchFetchedSchema);
        parser2.parse(printedAssertions);
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        List<Assertion> printedAndParsedAssertions = schema2.getAssertions();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions, equalTo(printedAndParsedAssertions));
    }
}
