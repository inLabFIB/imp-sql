package edu.upc.imp;

import edu.upc.imp.parser.SQLObjectSchemaParser;
import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;
import org.junit.jupiter.api.Test;
import utils.StatementsProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class TSQLIntegrationTest {
    @Test
    public void cv2Assertions() {
        String cv2Assertions = StatementsProvider.getCV2Assertions();
        SQLObjectSchemaParser parser1 = new SQLObjectSchemaParser(cv2Assertions);
        parser1.parse();
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        List<Assertion> expectedAssertions = schema1.getAssertions();

        SQLObjectSchemaVisitor printer = new SQLServerPrinter();
        String printedAssertions = String.join("\n\n", schema1.getAssertions().stream().map(a->a.<String>visit(printer)).toList());

        SQLObjectSchemaParser parser2 = new SQLObjectSchemaParser(printedAssertions);
        parser2.parse();
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Parsed assertions do not equal printed-then-parsed assertions",
            expectedAssertions.equals(schema2.getAssertions()));
    }
}
