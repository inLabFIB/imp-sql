package edu.upc.fib.inlab.imp.kse.sql.parser;

import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

public class GeneralSQLObjectSchemaParserTest {

    /** GENERAL **/

    @Test
    public void parsingOverAnExistingSchemaDoesntModifyIt() {
        String modifyingStatement = "CREATE VIEW viewName AS SELECT 1;";
        SQLObjectSchema originalSchema = new SQLObjectSchema();
        assertThat("Original schema is not empty.", originalSchema.equals(new SQLObjectSchema()));
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(originalSchema);
        parser.parse(modifyingStatement);
        SQLObjectSchema newSchema = parser.getSQLObjectSchema();
        assertThat("New schema doesn't contain changes applied.", !newSchema.equals(new SQLObjectSchema()));
        assertThat("Original schema was modified in the parsing process.", originalSchema.equals(new SQLObjectSchema()));
        assertThat("Original schema was modified in the parsing process.", !originalSchema.equals(newSchema));
    }


    @Test
    public void parsingUsingDifferentCallsProducesSameResultWhenIndependent() {
        String modifyingStatement1 = "CREATE VIEW view1 AS SELECT 1;";
        String modifyingStatement2 = "CREATE VIEW view2 AS SELECT 2;";

        SQLObjectSchemaParser parser1 = new SQLObjectSchemaParser();
        parser1.parse(modifyingStatement1+modifyingStatement2);
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        SQLObjectSchemaParser parser2 = new SQLObjectSchemaParser();
        parser2.parse(modifyingStatement1);
        parser2.parse(modifyingStatement2);
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Results are different if you parse same statements in a separate order", schema1.equals(schema2));
    }

}
