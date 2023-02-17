package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import org.junit.jupiter.api.Test;
import utils.StatementsProvider;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

}
