package edu.upc.fib.inlab.imp.kse.sql.core.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions.InvalidColumnReferenceException;
import edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions.InvalidOnJoinColumReferenceException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AliasValidatorTest {

    @Test
    void validAssertionAliases() {
        StandardSQLParser parser = new StandardSQLParser();

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
    void invalidColumnReference() {
        StandardSQLParser parser = new StandardSQLParser();

        String tableA = "CREATE TABLE a (col1 int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT a.col2;
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertThrows(InvalidColumnReferenceException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    void invalidAssertionAliasesMoreRequired() {
        StandardSQLParser parser = new StandardSQLParser();

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
    void invalidAssertionAliasesAmbiguousTable() {
        StandardSQLParser parser = new StandardSQLParser();

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
    void invalidAssertionAliasesRepeatedColumnAlias() {
        StandardSQLParser parser = new StandardSQLParser();

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
    void validSimpleAssertionWithWhereClause() {
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

        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(createTableStatement);
        parser.parse(createAssertionStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

        assertDoesNotThrow(() -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Test
    void validViewAliases() {
        StandardSQLParser parser = new StandardSQLParser();

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
    void invalidViewAliasesMoreRequired() {
        StandardSQLParser parser = new StandardSQLParser();

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

    @Test
    void TPCHSupplierNotCustomerWithWrongSchema() {
        StandardSQLParser parser = new StandardSQLParser();

        //LINEITEM is missing L_SUPPKEY
        String createTableStatement = """
            CREATE TABLE LINEITEM (L_ORDERKEY int);
            CREATE TABLE SUPPLIER (S_SUPPKEY int, S_NAME varchar(10));
            CREATE TABLE ORDERS (O_ORDERKEY int, O_CUSTKEY int);
            CREATE TABLE CUSTOMER (C_CUSTKEY int, C_NAME varchar(10));
            """;
        parser.parse(createTableStatement);

        // Object parsed from input string
        String createAssertionStatement = """
            CREATE ASSERTION supplierNotCustomer CHECK ( NOT EXISTS (
               SELECT *
               FROM LINEITEM AS l JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
                                   JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
                                   JOIN CUSTOMER AS c ON (o.O_CUSTKEY = c.C_CUSTKEY)
               WHERE s.S_NAME = c.C_NAME
            ));
            """;
        parser.parse(createAssertionStatement);

        SQLObjectSchema schema = parser.getSQLObjectSchema();
        SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();
        assertThrows(InvalidOnJoinColumReferenceException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
    }

    @Nested
    class InPredicateTests {
        @Test
        void CorrectInPredicate() {
            StandardSQLParser parser = new StandardSQLParser();

            String tableA = "CREATE TABLE a (col1 int, col2 int, col3 int)";
            parser.parse(tableA);

            // Object parsed from input string
            String assertion = """
            CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
                SELECT *
                FROM a
                WHERE 1 IN (a.col1, a.col2, a.col3, 3)
            ))""";
            parser.parse(assertion);

            SQLObjectSchema schema = parser.getSQLObjectSchema();

            SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

            assertDoesNotThrow(() -> validator.validateAliases(schema.getAssertions().get(0)));
        }

        @Test
        void IncorrectInPredicate() {
            StandardSQLParser parser = new StandardSQLParser();

            String tableA = "CREATE TABLE a (col1 int, col2 int, col3 int)";
            parser.parse(tableA);

            // Object parsed from input string
            String assertion = """
            CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
                SELECT *
                FROM a
                WHERE 1 IN (a.col1, a.col5)
            ))""";
            parser.parse(assertion);

            SQLObjectSchema schema = parser.getSQLObjectSchema();

            SQLObjectSchemaValidator validator = new SQLObjectSchemaValidator();

            assertThrows(InvalidColumnReferenceException.class, () -> validator.validateAliases(schema.getAssertions().get(0)));
        }
    }

}
