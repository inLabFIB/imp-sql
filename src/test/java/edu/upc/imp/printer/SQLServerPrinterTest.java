package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
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
    @Disabled
    void printSPJView() {
        View view = new View(
            "FirstView",
            new TableExpression()
        );

        String sqlSyntax = view.visit(new SQLServerPrinter());

        String expectedSqlSyntax = "CREATE VIEW FirstView AS SELECT 1 AS idx, table.column AS val FROM table1, table2;";
        assertThat(sqlSyntax, is(expectedSqlSyntax));
    }
}
