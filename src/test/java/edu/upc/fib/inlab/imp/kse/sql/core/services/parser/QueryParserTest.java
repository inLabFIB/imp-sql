package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.CrossJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryParserTest {

    @Test
    void queryWithoutSelectItemsShouldThrowException() {
        // Object parsed from input string
        String query = "SELECT  FROM tableA AS T0";
        StandardSQLParser parser = new StandardSQLParser();
        assertThatThrownBy(() -> parser.parse(query), "Query with no select items should throw an exception", IMPSqlException.class);
    }

    @Test
    void parseSimpleQueryStatement() {
        // Object parsed from input string
        String basicQuery = "SELECT 1";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(basicQuery);
        List<Query> queries = parser.getQueries();

        // Object built directly in java
        Query expectedQuery = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))), null, null);

        assertThat(queries).hasSize(1).first().isEqualTo(expectedQuery);
    }

    @Test
    void parseSimpleQueriesStatement() {
        // Object parsed from input string
        String basicQuery = """
            SELECT 1;
            SELECT 2;
            """;
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(basicQuery);
        List<Query> queries = parser.getQueries();

        // Object built directly in java
        Query expectedQuery1 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))), null, null);
        Query expectedQuery2 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(2))), null, null);

        assertThat(queries).hasSize(2).first().isEqualTo(expectedQuery1);
        assertThat(queries).last().isEqualTo(expectedQuery2);
    }

    @Test
    void parseAssertionWithAliases() {
        StandardSQLParser parser = new StandardSQLParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = "SELECT a.b, d.e FROM a, (SELECT a.c as e FROM a) as d";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();
        List<Query> queries = parser.getQueries();

        // Object built directly in java
        Query expectedQuery = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("a", "b")),
                new AliasableSelectItem(new ColumnReference("d", "e"))),
            new CrossJoin(
                new TableReference(schema.getTables().get(0)),
                new TableExpression(
                    List.of(new AliasableSelectItem(new ColumnReference("a", "c"), "e")),
                    new TableReference(schema.getTables().get(0)),
                    null,
                    "d"
                )
            ),
            null
        );

        assertThat(queries).hasSize(1).first().isEqualTo(expectedQuery);
    }
}
