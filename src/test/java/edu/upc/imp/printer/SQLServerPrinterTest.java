package edu.upc.imp.printer;

import edu.upc.imp.parser.SQLObjectSchemaParser;
import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SQLServerPrinterTest {

    /** ASSERTIONS **/

    @Test
    public void parseSimpleCreateAssertionStatement() {
        // Object built directly in java
        Assertion assertion = new Assertion(
            "assertionName",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLInteger(1))),
                    null, null
                )
            ))
        );

        String expectedAssertion = "CREATE ASSERTION assertionName CHECK ( NOT ( EXISTS ( SELECT 1 ) ) );";
        assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
    }

    /** VIEWS **/

    @Test
    public void parseCrateViewStatementWithColumnNames() {
        // Object built directly in java
        View view = new View(
            "viewName", null, List.of("col1"),
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLInteger(1))),
                null, null
            ));

        String expectedView = "CREATE VIEW viewName ( col1 ) AS ( SELECT 1 );";
        assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
    }

    @Test
    public void parseCrateViewStatement() {
        // Object built directly in java
        View view = new View(
            "viewName",
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLInteger(1))),
                null, null
            ));

        String expectedView = "CREATE VIEW viewName AS ( SELECT 1 );";
        assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
    }

    /** SELECTS **/

    //SIMPLE SELECT
    @Test
    public void parseSelectStatement() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("pk")),
                new AliasableSelectItem(new ColumnReference("attr"))),
            new TableReference(new FullTableName("myTable")),
            new ComparisonPredicate(
                ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference("pk"),
                new SQLInteger(1)),
            true
        );

        String expectedAssertion = "SELECT pk, attr FROM myTable WHERE pk = 1;";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
    }

    //JOINS
    @Test
    public void parseSelectWithJoinClause() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference(new FullTableName("A"),"attr1")),
                new AliasableSelectItem(new ColumnReference(new FullTableName("B"),"attr2"))
            ), new OnJoin(OnJoin.JoinOperator.INNER,
            new TableReference(new FullTableName("sameSchema", "A")),
            new TableReference(new FullTableName("sameSchema", "B")),
            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference( new FullTableName("A"),"fk"),
                new ColumnReference( new FullTableName("B"),"pk"))),
            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference( new FullTableName("B"),"attr3"),
                new SQLFloat(1.1f)),
            true
        );

        String expectedSelect = "SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON ( A.fk = B.pk ) WHERE B.attr3 = 1.1;";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }

    //SELECT WITH RECURSIVE SELECT
    @Test
    public void parseSelectWithRecursiveSelectAndFrom() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("b"), "money"),
                new AliasableSelectItem(
                    new TableExpression(
                        List.of(new AliasableSelectItem(new ColumnReference("c"))),
                        new TableReference(new FullTableName("otherTable")),null))),
            new TableExpression(
                List.of(
                    new AliasableSelectItem(new ColumnReference("a")),
                    new AliasableSelectItem(new ColumnReference("b"))),
                new TableReference(new FullTableName("myTable")),
                null),
            new ComparisonPredicate(
                ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference(null, "a"),
                new SQLInteger(1)),
            true);

        String expectedAssertion = "SELECT b AS money, ( SELECT c FROM otherTable ) FROM ( SELECT a, b FROM myTable ) WHERE a = 1;";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
    }

    //MULTIPLE JOINS (check priority)
    @Test
    public void parseSelectWithMultipleJoinClausesOfPriority() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(new Asterisk()),
            new CrossJoin(
                new CrossJoin(
                    new TableReference(new FullTableName("A")),
                    new OnJoin(
                        OnJoin.JoinOperator.INNER,
                        new TableReference(new FullTableName("B")),
                        new TableReference(new FullTableName( "C")),
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new ColumnReference( new FullTableName("B"),"fk"),
                            new ColumnReference( new FullTableName("C"),"pk")
                        )
                    )
                ),
                new CrossJoin(
                    new TableReference(new FullTableName("D")),
                    new TableReference(new FullTableName("E"))
                )
            ),
            null,
            true
        );

        String expectedSelect = "SELECT * FROM A CROSS JOIN B INNER JOIN C ON ( B.fk = C.pk ) CROSS JOIN ( D CROSS JOIN E );";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }

    //PREDICATES (not and,...)
    @Test
    public void parseSelectStatementWithComplexPredicate() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(new Asterisk(), new Asterisk()),
            new TableReference(new FullTableName("myTable")),
            new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new SQLInteger(1),
                    new SQLInteger(0)
                ),
                new PredicateOperation(
                    PredicateOperation.PredicateOperator.AND,
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new SQLString("SQLCommonSense"),
                        new SQLString("")
                    ),
                    new NotOperation(new NotOperation(new NotOperation(
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new SQLInteger(0),
                            new SQLInteger(0)
                        )
                    )))
                )
            ),
            true
        );

        String expectedSelect = "SELECT *, * FROM myTable WHERE 1 = 0 AND 'SQLCommonSense' = '' AND NOT ( NOT ( NOT ( 0 = 0 ) ) );";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }
}
