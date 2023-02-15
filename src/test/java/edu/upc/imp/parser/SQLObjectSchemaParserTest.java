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
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(StatementsProvider.getTPCHAtLeastOneItemAssertion());
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();
        assertNotNull(schema);
    }

}
