package edu.upc.imp.sql.parser;

import edu.upc.imp.sql.sqlobjectschema.Assertion;
import edu.upc.imp.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sql.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.imp.sql.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.imp.sql.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.imp.sql.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.imp.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class AssertionsSQLObjectSchemaParserTest {


    @Test
    public void parseSimpleCreateAssertionStatement() {
        // Object parsed from input string
        String basicAssertion = "CREATE ASSERTION assertionName CHECK ( NOT EXISTS ( SELECT 1))";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicAssertion);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Assertion expectedAssertion = new Assertion(
            "assertionName",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                )
            ))
        );

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));
    }
}
