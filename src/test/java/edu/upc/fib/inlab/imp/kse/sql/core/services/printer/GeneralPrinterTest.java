package edu.upc.fib.inlab.imp.kse.sql.core.services.printer;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLSchemaMother;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.SetOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer.SQLServerPrinter;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.SetOperation.SetOperator.*;
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

        assertThat(schema.getPrintedSchemaObjects(new StandardSQLPrinter()), is(expectedOutput));
    }

    @Nested
    class SetOperationTests {

        private static Stream<Arguments> providesSetOperators() {
            return Stream.of(
                Arguments.of(UNION, false, "UNION"),
                Arguments.of(UNION, true, "UNION ALL"),
                Arguments.of(EXCEPT, false, "EXCEPT"),
                Arguments.of(INTERSECT, false, "INTERSECT")
            );
        }

        @ParameterizedTest
        @MethodSource("providesSetOperators")
        void printSetOperators(SetOperation.SetOperator operator, boolean repeatedValues, String setOperator) {
            // Object built directly in java
            Query union = new SetOperation(operator, repeatedValues,
                                           new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(1)))),
                                           new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(2))))
            );
            String expectedUnion = "( ( SELECT 1 ) " + setOperator + " ( SELECT 2 ) )";
            MatcherAssert.assertThat(union.visit(new SQLServerPrinter()), is(expectedUnion));
        }

        @Test
        void printMultipleUnions() {
            Query query1 = new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(1))));
            Query query2 = new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(2))));
            Query query3 = new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(3))));
            // Object built directly in java
            Query union = new SetOperation(UNION, false,
                                           new SetOperation(EXCEPT, false, query1, query2),
                                           query3
            );
            String expectedUnion = "( ( ( SELECT 1 ) EXCEPT ( SELECT 2 ) ) UNION ( SELECT 3 ) )";
            MatcherAssert.assertThat(union.visit(new SQLServerPrinter()), is(expectedUnion));
        }

    }

}
