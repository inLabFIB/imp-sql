package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SQLServerPrinterTest {
    @Test
    void printSPJAssertion() {
        Assertion assertion = new Assertion(
            "FirstAssertion",
            new ComparisonPredicate(
                ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference(new FullTableName("table"), "column"),
                new SQLInteger(2)
            )
        );
        String sqlSyntax = assertion.visit(new SQLServerPrinter());

        String expectedSqlSyntax = "CREATE ASSERTION FirstAssertion CHECK ( table.column = 2 );";
        assertThat(sqlSyntax, is(expectedSqlSyntax));
    }

    @Test
    void printSPJView() {
        View view = new View(
            "FirstView",
            new TableExpression(
                List.of(new AliasableSelectItem("idx", new SQLString("Id-1")), new AliasableSelectItem("val", new ColumnReference( new FullTableName("table"), "column"))),
                new CrossJoin(new TableReference(new FullTableName("table1"), "T1"), new TableReference(new FullTableName("table2"), null)),
                new PredicateOperation(
                    PredicateOperation.PredicateOperator.AND,
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new SQLInteger(1),
                        new SQLFloat(1.0f)
                    ),
                    new NotOperation(new ExistsPredicate(
                        new TableExpression(
                            List.of(new AliasableSelectItem(null, new SQLInteger(1))),null, null, null
                        )
                    ))
                ), null)
        );

        String sqlSyntax = view.visit(new SQLServerPrinter());

        String expectedSqlSyntax = "CREATE VIEW FirstView AS SELECT 'Id-1' AS idx, table.column AS val FROM table1 AS T1 CROSS JOIN table2 WHERE 1 = 1.0 AND NOT ( EXISTS ( SELECT 1 ) );";
        assertThat(sqlSyntax, is(expectedSqlSyntax));
    }
}
