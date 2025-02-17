package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.StandardSQLParser;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SetOperationTest {

    @Nested
    class SetOperationCreationRestrictionsTest {
        @Test
        void shouldCreateUnionSetOperation_whenQueriesHaveEqualSelectClauseArity() {
            Query query = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))));

            SetOperation so = new SetOperation(SetOperation.SetOperator.UNION, false, query, query);

            assertThat(so).isNotNull();
        }

        @Test
        void shouldNotCreateUnionSetOperation_whenQueriesHaveDifferentSelectClauseArity() {
            Query query1 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))));
            Query query2 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1)),
                                                       new AliasableSelectItem(new SQLPrimitiveInteger(1))));

            assertThatThrownBy(() -> new SetOperation(SetOperation.SetOperator.UNION, false, query1, query2))
                .isInstanceOf(IMPSqlException.class);
        }
    }

    @Nested
    class SetOperationOfferedReferencesTest {

        @Test
        void shouldReturnLeftExpressionOfferedReferences() {
            String union = """
                SELECT 1 AS a, 2 AS b
                UNION
                SELECT 3 AS c, 4 AS d
                """;
            StandardSQLParser parser = new StandardSQLParser();
            parser.parse(union);
            Query query = parser.getQueries().get(0);

            assertThat(query.getOfferedReferences())
                .isEqualTo(((SetOperation) query).getLeftExpression().getOfferedReferences());
        }

    }


}
