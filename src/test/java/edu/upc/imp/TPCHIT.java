package edu.upc.imp;

import edu.upc.imp.fetcher.SQLObjectSchemaFetcher;
import edu.upc.imp.parser.SQLObjectSchemaParser;
import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.Assertion;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import utils.TintinAssertionsProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@Disabled
public class TPCHIT {

    private SQLObjectSchemaFetcher tpch_fetcher;

    @BeforeEach
    public void fetcherSetUp() {
        tpch_fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "tpch_db",
            List.of("user_schema"),
            "SA",
            "PasswordO1.",
            SQLObjectSchemaFetcher.DBType.SQLServer
        );
    }

    @Test
    public void fetchingTpchTables() {
        tpch_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = tpch_fetcher.getSQLObjectSchema();

        assertThat("Fetcher didn't correctly fetch the TPCH schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());
    }

   @Test
    public void parsingCV2AssertionsWithFetchedTables() {
        tpch_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = tpch_fetcher.getSQLObjectSchema();

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser.parse(TintinAssertionsProvider.getCV2Assertions());
        SQLObjectSchema cv2SchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !cv2SchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    public void cv2Assertions() {
        tpch_fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = tpch_fetcher.getSQLObjectSchema();

        SQLObjectSchemaParser parser1 = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser1.parse(TintinAssertionsProvider.getCV2Assertions());
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", schema1.getAssertions().stream().map(a->a.<String>visit(printer)).toList());

        SQLObjectSchemaParser parser2 = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser2.parse(printedAssertions);
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions.equals(schema2.getAssertions()));
    }
}
