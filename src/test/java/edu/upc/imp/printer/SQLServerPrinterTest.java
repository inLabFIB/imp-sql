package edu.upc.imp.printer;

import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.imp.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.imp.sqlobjectschema.boolean_expressions.PredicateOperation;
import edu.upc.imp.sqlobjectschema.relational_expressions.*;
import edu.upc.imp.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.imp.sqlobjectschema.selection_expressions.Asterisk;
import edu.upc.imp.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveFloat;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveString;
import org.junit.jupiter.api.Test;
import utils.SchemasProvider;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

//TODO: separate in different test  classes?
class SQLServerPrinterTest {

    /** TABLE **/

    //TODO: add table tests

    /** ASSERTIONS **/

    @Test
    public void printSimpleCreateAssertionStatement() {
        // Object built directly in java
        Assertion assertion = new Assertion(
            "assertionName",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                )
            ))
        );

        String expectedAssertion = "CREATE ASSERTION assertionName CHECK ( NOT ( EXISTS ( SELECT 1 ) ) );";
        assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
    }

    /** VIEWS **/

    @Test
    public void printCrateViewStatementWithColumnNames() {
        // Object built directly in java
        View view = new View(
            "viewName", null, List.of("col1"),
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                null, null
            ));

        String expectedView = "CREATE VIEW viewName ( col1 ) AS ( SELECT 1 );";
        assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
    }

    @Test
    public void printCrateViewStatement() {
        // Object built directly in java
        View view = new View(
            "viewName",
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                null, null
            ));

        String expectedView = "CREATE VIEW viewName AS ( SELECT 1 );";
        assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
    }

    /** VIEWS **/
    /** All prints contain parenthesis and no ending ';' because they are interpreted as subqueries. **/

    //SIMPLE SELECT
    @Test
    public void printSelectStatement() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("a")),
                new AliasableSelectItem(new ColumnReference("b"))),
            new TableReference(SchemasProvider.getMyTableSchemaTables().get(0)),
            new ComparisonPredicate(
                ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference("a"),
                new SQLPrimitiveInteger(1))
        );

        String expectedAssertion = "( SELECT a, b FROM myTable WHERE a = 1 )";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
    }

    //JOINS
    @Test
    public void printSelectWithJoinClause() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("A","attr1")),
                new AliasableSelectItem(new ColumnReference("B","attr2"))
            ), new OnJoin(OnJoin.JoinOperator.INNER,
            new TableReference(SchemasProvider.getABSchemaTables().get(0)),
            new TableReference(SchemasProvider.getABSchemaTables().get(1)),
            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference("A","fk"),
                new ColumnReference("B","pk"))),
            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference("B","attr3"),
                new SQLPrimitiveFloat(1.1f))
        );

        String expectedSelect = "( SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON ( A.fk = B.pk ) WHERE B.attr3 = 1.1 )";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }

    //SELECT WITH RECURSIVE SELECT
    @Test
    public void printSelectWithRecursiveSelectAndFrom() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(
                new AliasableSelectItem(new ColumnReference("b"), "money"),
                new AliasableSelectItem(
                    new TableExpression(
                        List.of(new AliasableSelectItem(new ColumnReference("c"))),
                        new TableReference(SchemasProvider.getMyTableSchemaTables().get(1)),null))),
            new TableExpression(
                List.of(
                    new AliasableSelectItem(new ColumnReference("a")),
                    new AliasableSelectItem(new ColumnReference("b"))),
                new TableReference(SchemasProvider.getMyTableSchemaTables().get(0)),
                null),
            new ComparisonPredicate(
                ComparisonPredicate.ComparisonOperator.EQ,
                new ColumnReference(null, "a"),
                new SQLPrimitiveInteger(1))
        );

        String expectedAssertion = "( SELECT b AS money, ( SELECT c FROM otherTable ) FROM ( SELECT a, b FROM myTable ) WHERE a = 1 )";
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
                    new TableReference(SchemasProvider.getJoinsSchemaTables().get(0)),
                    new OnJoin(
                        OnJoin.JoinOperator.INNER,
                        new TableReference(SchemasProvider.getJoinsSchemaTables().get(1)),
                        new TableReference(SchemasProvider.getJoinsSchemaTables().get(2)),
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new ColumnReference("B","B_fk"),
                            new ColumnReference("C","C_pk")
                        )
                    )
                ),
                new CrossJoin(
                    new TableReference(SchemasProvider.getJoinsSchemaTables().get(3)),
                    new TableReference(SchemasProvider.getJoinsSchemaTables().get(4))
                )
            ),
            null
        );

        String expectedSelect = "( SELECT * FROM A CROSS JOIN B INNER JOIN C ON ( B.B_fk = C.C_pk ) CROSS JOIN ( D CROSS JOIN E ) )";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }

    //PREDICATES (not and,...)
    @Test
    public void parseSelectStatementWithComplexPredicate() {
        // Object built directly in java
        Query select = new TableExpression(
            List.of(new Asterisk(), new Asterisk()),
            new TableReference(SchemasProvider.getMyTableSchemaTables().get(0)),
            new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new SQLPrimitiveInteger(1),
                    new SQLPrimitiveInteger(0)
                ),
                new PredicateOperation(
                    PredicateOperation.PredicateOperator.AND,
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new SQLPrimitiveString("SQLCommonSense"),
                        new SQLPrimitiveString("")
                    ),
                    new NotOperation(new NotOperation(new NotOperation(
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new SQLPrimitiveInteger(0),
                            new SQLPrimitiveInteger(0)
                        )
                    )))
                )
            )
        );

        String expectedSelect = "( SELECT *, * FROM myTable WHERE 1 = 0 AND 'SQLCommonSense' = '' AND NOT ( NOT ( NOT ( 0 = 0 ) ) ) )";
        assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }
}
