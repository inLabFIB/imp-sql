package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ParserThrowsExceptionsTest {

    @Test
    void incorrectTableCreationThrowsException() {
       String createTable = """
            CREATE TABLE tableName (col int,
            """;
        StandardSQLParser parser = new StandardSQLParser();

        assertThrows(Exception.class, () -> parser.parse(createTable));
    }

    @Test
    void incorrectTableCreationThrowsException2() {
        String createTable = """
            CREATE ASSERT
            """;
        StandardSQLParser parser = new StandardSQLParser();

        assertThrows(Exception.class, () -> parser.parse(createTable));
    }
}
