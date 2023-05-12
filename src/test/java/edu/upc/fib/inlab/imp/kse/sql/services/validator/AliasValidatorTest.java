package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertDoesNotThrow(() -> validator.validateAliases(schema.getAssertions().get(0)));
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

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(RuntimeException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
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

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(RuntimeException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
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

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(RuntimeException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void validSimpleAssertionWithWhereClause() {
        String createTableStatement = """
            CREATE TABLE tableA (
                col1 int,
                col2 int,
                col3 int,
            );
            """;

        String createAssertionStatement = """
            CREATE ASSERTION assertion1 CHECK ( NOT ( EXISTS (
            	SELECT tableA.col1
            	FROM tableA
            	WHERE tableA.col1 = 1
            )));
            """;

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTableStatement);
        parser.parse(createAssertionStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertDoesNotThrow(() -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void validViewAliases() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String view = """
        CREATE VIEW viewName AS SELECT a.b, d.e FROM a, (SELECT a.c as e FROM a) as d""";
        parser.parse(view);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(RuntimeException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    public void invalidViewAliasesMoreRequired() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String view = """
        CREATE VIEW viewName AS SELECT a.f, d.e FROM a, (SELECT a.c as e FROM a) as d""";
        parser.parse(view);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(RuntimeException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));

    }
}
