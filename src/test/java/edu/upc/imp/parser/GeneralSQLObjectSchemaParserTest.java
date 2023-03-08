package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.imp.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.imp.sqlobjectschema.relational_expressions.*;
import edu.upc.imp.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLFloat;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLInt;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class GeneralSQLObjectSchemaParserTest {

    /** GENERAL **/

    //TODO: parser needs to store a input schema copy
    @Disabled
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
    public void parsingUsingDifferentCallsProducesSameResultWhenIndependant() {
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
