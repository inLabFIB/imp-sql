package edu.upc.imp.parser;

import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SQLObjectSchemaParserTest {

    public SQLObjectSchemaParserTest(){

    }

    @Test
    public void shouldLoadTPCHAssertion1() {
        String basicAssertion = "CREATE ASSERTION assertionName CHECK ( NOT EXISTS ( SELECT 1))";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicAssertion);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();
        assert(schema.getAssertions().size() == 1);
    }

    @Test
    public void crateViewStatementGeneratesCorrectObject() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName AS SELECT table1.col1 AS c1 FROM table1, table2;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicView);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();
        // Object built directly in java
        View expectedView = new View("viewName", new TableExpression(
            List.of(new AliasableSelectItem("c1", new ColumnReference(new FullTableName("table1"),"col1"))),
            new CrossJoin(new TableReference(new FullTableName("table1"), null), new TableReference(new FullTableName("table2"), null)),
            null, null
        ));
        // Compare the string representation of both (for some "toString" visitor)
        assertThat(schema.getViews().get(0).visit(new SQLServerPrinter()), is(expectedView.visit(new SQLServerPrinter())));
    }

}
