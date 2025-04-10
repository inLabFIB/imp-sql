package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLSchemaMother;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.upc.fib.inlab.imp.kse.sql.core.utils.SchemasProvider.getMyTableSchemaTables;
import static org.assertj.core.api.Assertions.assertThat;

class TableExpressionTest {

    @Nested
    class GetNumberOfReturnColumnsMethodTest {

        @Test
        void shouldReturnOneWhenOneColumnIsReturned() {
            TableExpression te = new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(1))));

            assertThat(te.getNumberOfReturnColumns()).isEqualTo(1);
        }

        @Test
        void shouldReturnTwoWhenTwoColumnIsReturned() {
            TableExpression te = new TableExpression(List.of(SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(1)),
                SQLSchemaMother.createAliasableSelectItem(new SQLPrimitiveInteger(1))));

            assertThat(te.getNumberOfReturnColumns()).isEqualTo(2);
        }

        @Test
        void shouldReturnTwoWhenTwoColumnIsReturnedWithAliases() {
            String union = """
                SELECT 1 AS a, 2 AS b
                """;
            StandardSQLParser parser = new StandardSQLParser();
            parser.parse(union);
            Query query = parser.getQueries().get(0);

            assertThat(query.getNumberOfReturnColumns()).isEqualTo(2);
        }

        @Test
        void shouldReturnNumberOfOfferedColumns_whenUsingAnAsterisk() {
            TableExpression te = new TableExpression(List.of(new Asterisk()), new TableReference(getMyTableSchemaTables().get(0)));

            assertThat(te.getNumberOfReturnColumns()).isEqualTo(2);
        }

        @Test
        void shouldReturnMoreNumberOfOfferedColumns_whenUsingMultipleAsterisk() {
            TableExpression te = new TableExpression(List.of(new Asterisk(), new Asterisk()), new TableReference(getMyTableSchemaTables().get(0)));

            assertThat(te.getNumberOfReturnColumns()).isEqualTo(4);
        }


    }

}
