package edu.upc.fib.inlab.imp.kse.sql.core.services.printer;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer.SQLServerPrinter;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class GeneralPrinterTest {

    /* SQL SERVER */
    @Test
    void assertSchemaPrintsCorrectly() {
        String statements = """
            CREATE TABLE A (col1 int, col2 int);
            CREATE TABLE B (col3 int);
            CREATE ASSERTION assert CHECK (NOT (EXISTS (SELECT B.col2 FROM B)));
            """;

        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(statements);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        String expectedOutput = """
            <<TABLES>>
            CREATE TABLE A ( col1 INT, col2 INT );
            CREATE TABLE B ( col3 INT );
            
            <<ASSERTIONS>>
            CREATE ASSERTION assert CHECK ( NOT ( EXISTS ( SELECT B.col2 FROM B ) ) );
            
            """;

        assertThat(schema.getPrintedSchemaObjects(new SQLServerPrinter()), is(expectedOutput));
    }


}
