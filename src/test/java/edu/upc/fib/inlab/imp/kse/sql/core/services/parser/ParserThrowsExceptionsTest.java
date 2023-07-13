package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserThrowsExceptionsTest {

    @Test
    public void incorrectTableCreationThrowsException() {
       String createTable = """
            CREATE TABLE tableName (col int,
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(Exception.class, () -> parser.parse(createTable));
    }

    @Test
    public void incorrectTableCreationThrowsException2() {
        String createTable = """
            CREATE ASSERT
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(Exception.class, () -> parser.parse(createTable));
    }
}
