package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class GeneralStandardSQLParserTest {

    /** GENERAL **/

    @Test
    void parsingOverAnExistingSchemaDoesntModifyIt() {
        String modifyingStatement = "CREATE VIEW viewName AS SELECT 1;";
        SQLObjectSchema originalSchema = new SQLObjectSchema();
        assertThat("Original schema is not empty.", originalSchema.equals(new SQLObjectSchema()));
        StandardSQLParser parser = new StandardSQLParser(originalSchema);
        parser.parse(modifyingStatement);
        SQLObjectSchema newSchema = parser.getSQLObjectSchema();
        assertThat("New schema doesn't contain changes applied.", !newSchema.equals(new SQLObjectSchema()));
        assertThat("Original schema was modified in the parsing process.", originalSchema.equals(new SQLObjectSchema()));
        assertThat("Original schema was modified in the parsing process.", !originalSchema.equals(newSchema));
    }


    @Test
    void parsingUsingDifferentCallsProducesSameResultWhenIndependent() {
        String modifyingStatement1 = "CREATE VIEW view1 AS SELECT 1;";
        String modifyingStatement2 = "CREATE VIEW view2 AS SELECT 2;";

        StandardSQLParser parser1 = new StandardSQLParser();
        parser1.parse(modifyingStatement1+modifyingStatement2);
        SQLObjectSchema schema1 = parser1.getSQLObjectSchema();

        StandardSQLParser parser2 = new StandardSQLParser();
        parser2.parse(modifyingStatement1);
        parser2.parse(modifyingStatement2);
        SQLObjectSchema schema2 = parser2.getSQLObjectSchema();

        assertThat("Results are different if you parse same statements in a separate order", schema1.equals(schema2));
    }

}
