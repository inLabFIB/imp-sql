package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Disabled;
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
                new ColumnReference("table", "column"),
                new Constant("2")
            )
        );
        String sqlSyntax = assertion.visit(new SQLServerPrinter());

        String expectedSqlSyntax = "CREATE ASSERTION FirstAssertion CHECK ( table.column = 2 );";
        assertThat(sqlSyntax, is(expectedSqlSyntax));
    }

    @Test
    void printSPJView() {
        List<String> nullStringList = new ArrayList<>();
        nullStringList.add(null);
        View view = new View(
            "FirstView",
            new TableExpression(
                List.of(new Constant("1"), new ColumnReference("table", "column")),
                List.of("idx", "val"),
                new CrossJoin(new TableReference("table1", "T1"), new TableReference("table2", null)),
                new PredicateOperation(
                    PredicateOperation.PredicateOperator.AND,
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new Constant("1"),
                        new Constant("1")
                    ),
                    new NotOperation(new ExistsPredicate(
                        new TableExpression(
                            List.of(new Constant("1")), nullStringList, null, null, null
                        )
                    ))
                ), null)
        );

        String sqlSyntax = view.visit(new SQLServerPrinter());

        String expectedSqlSyntax = "CREATE VIEW FirstView AS SELECT 1 AS idx, table.column AS val FROM table1 AS T1 CROSS JOIN table2 WHERE 1 = 1 AND NOT ( EXISTS ( SELECT 1 ) );";
        assertThat(sqlSyntax, is(expectedSqlSyntax));
    }
}
