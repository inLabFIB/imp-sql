package edu.upc.fib.inlab.imp.kse.sql.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
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
}
