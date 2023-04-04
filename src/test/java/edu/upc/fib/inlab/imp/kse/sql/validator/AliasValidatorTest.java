package edu.upc.fib.inlab.imp.kse.sql.validator;

import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.services.validator.Validator;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import org.junit.jupiter.api.Test;

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
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT a.f, d.e FROM a, (SELECT a.c as e FROM a) as d
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Validator validator = new Validator();

        assertThat("Aliases were considered correct when they were not",
            !validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void invalidAssertionAliasesAmbiguousTable() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT b FROM a, (SELECT a.b FROM a) as d
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Validator validator = new Validator();

        assertThat("Aliases were considered correct when they were not",
            !validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void invalidAssertionAliasesRepeatedColumnAlias() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT * FROM a, (SELECT * FROM a) as a
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Validator validator = new Validator();

        assertThat("Aliases were considered correct when they were not",
            !validator.validateAliases(schema.getAssertions().get(0)));
    }
}
