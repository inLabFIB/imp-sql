package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.services.validator.exceptions.InvalidColumnReferenceException;
import edu.upc.fib.inlab.imp.kse.sql.services.validator.exceptions.InvalidOnJoinColumReferenceException;
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
    public void invalidColumnReference() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

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

    @Test
    public void TPCHSupplierNotCustomerWithWrongSchema() {
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

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
}
