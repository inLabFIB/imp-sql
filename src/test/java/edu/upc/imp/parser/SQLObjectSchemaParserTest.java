package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class SQLObjectSchemaParserTest {

    /** ASSERTIONS **/

    @Test
    public void parseSimpleCreateAssertionStatement() {
        // Object parsed from input string
        String basicAssertion = "CREATE ASSERTION assertionName CHECK ( NOT EXISTS ( SELECT 1))";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicAssertion);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Assertion expectedAssertion = new Assertion(
            new FullTableName("assertionName"),
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLInteger(1))),
                    null, null
                )
            ))
        );

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));
    }

    /** VIEWS **/

    @Test
    public void parseCrateViewStatementWithColumnNames() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName (col1) AS SELECT 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicView);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        View expectedView = new View(
            new FullTableName("viewName"), List.of("col1"),
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLInteger(1))),
                null, null
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    @Test
    public void parseCrateViewStatement() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName AS SELECT 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicView);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        View expectedView = new View(
            new FullTableName("viewName"),
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLInteger(1))),
                null, null
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    /** SELECTS **/

    //SIMPLE SELECT
    @Test
    public void parseSelectStatement() {
        // Object parsed from input string
        String basicSelect = "SELECT pk, attr FROM myTable WHERE pk = 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicSelect);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Query expectedSelect = new TableExpression(
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

        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
    }

    //JOINS
    @Test
    public void parseSelectWithJoinClause() {
        // Object parsed from input string
        String selectWithJoin = "SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON (A.fk = B.pk) WHERE B.attr3 = 1.1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(selectWithJoin);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Query expectedSelect = new TableExpression(
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

        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
    }

    //SELECT WITH RECURSIVE SELECT
    @Test
    public void parseSelectWithRecursiveSelectAndFrom() {
        // Object parsed from input string
        String basicSelect = "SELECT b AS money, (SELECT c FROM otherTable) FROM (SELECT a, b FROM myTable) WHERE a = 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicSelect);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Query expectedSelect = new TableExpression(
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
            true
        );

        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
    }

    //MULTIPLE JOINS (check priority)
    @Test
    public void parseSelectWithMultipleJoinClausesOfPriority() {
        // Object parsed from input string
        String selectWithJoins = "SELECT * FROM A, B INNER JOIN C ON (B.fk = C.pk), (D CROSS JOIN E);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(selectWithJoins);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Query expectedSelect = new TableExpression(
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

        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
    }

    //PREDICATES (not and,...)
    @Test
    public void parseSelectStatementWithComplexPredicate() {
        // Object parsed from input string
        String basicSelect = "SELECT *, * FROM myTable WHERE 1 = 0 AND ('SQLCommonSense' = '' AND NOT NOT (NOT 0 = 0));";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicSelect);
        parser.parse();
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Query expectedSelect = new TableExpression(
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

        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
    }

}
