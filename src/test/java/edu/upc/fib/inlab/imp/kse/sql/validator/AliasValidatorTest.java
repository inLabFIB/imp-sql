package edu.upc.fib.inlab.imp.kse.sql.validator;

import edu.upc.fib.inlab.imp.kse.sql.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class AliasValidatorTest {

    @Test
    public void validAssertionAliases() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT a.b, d.e FROM a, (SELECT a.c as e FROM a) as d
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Validator validator = new Validator();

        assertThat("Incorrect aliases", validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void invalidAssertionAliasesMoreRequired() {

    }
}
