package edu.upc.imp;

import edu.upc.imp.fetcher.SQLObjectSchemaFetcher;
import edu.upc.imp.parser.SQLObjectSchemaParser;
import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;
import org.junit.jupiter.api.Test;
import utils.TintinAssertionsProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class TSQLIntegrationTest {

    //FIXME: test doesn't work because the parser doesn't infer the schema and database name of the referenced tables
    // in the assertions. Could be fixed adding this information in the statements or changing the parser behaviour.
    @Test
    public void parsingCV2AssertionsWithFetchedTables() {
        SQLObjectSchemaFetcher cv2Fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "cv2_db",
            "user_schema",
            "SA",
            "PasswordO1.",
            SQLObjectSchemaFetcher.DBType.SQLServer
        );
        cv2Fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = cv2Fetcher.getSQLObjectSchema();

        assertThat("Fetcher didn't correctly fetch the CV2 schema tables.",
            !cv2FetchedSchema.getTables().isEmpty());

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(cv2FetchedSchema);
        parser.parse(TintinAssertionsProvider.getCV2Assertions());
        SQLObjectSchema cv2SchemaWithAssertions = parser.getSQLObjectSchema();

        assertThat("Assertions were not parsed correctly.",
            !cv2SchemaWithAssertions.getAssertions().isEmpty());
    }

    @Test
    public void cv2Assertions() {
        SQLObjectSchemaFetcher cv2Fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "cv2_db",
            "user_schema",
            "SA",
            "PasswordO1.",
            SQLObjectSchemaFetcher.DBType.SQLServer
        );
        cv2Fetcher.fetch();
        SQLObjectSchema cv2FetchedSchema = cv2Fetcher.getSQLObjectSchema();

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
