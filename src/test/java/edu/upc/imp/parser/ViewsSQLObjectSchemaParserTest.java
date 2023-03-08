package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.View;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.imp.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.imp.sqlobjectschema.relational_expressions.TableReference;
import edu.upc.imp.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLInt;
import edu.upc.imp.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class ViewsSQLObjectSchemaParserTest {

    //TODO: maybe create a tables provider
    private final String myTableCreateTableStatement = """
        CREATE TABLE myTable (
            a int,
            b int,
        );
                
        CREATE TABLE otherTable (
            c int,
            d int,
        );
        """;

    @Test
    public void parseTrivialCrateViewStatementWithColumnNames() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName (col1) AS SELECT 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicView);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            null,
            List.of("col1"),
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                null, null
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    @Test
    public void parseTrivialCrateViewStatement() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName AS SELECT 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicView);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                null, null
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    //TODO: remove tests, and change them with view tests


    @Test
    public void parseCreateViewStatementWithSimpleSelect() {
        // Object parsed from input string
        String basicSelect = "CREATE VIEW viewName AS SELECT a, b FROM myTable WHERE a = 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(myTableCreateTableStatement);
        parser.parse(basicSelect);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table myTable = new Table(
            "myTable",
            List.of(
                new Attribute("a", new SQLInt()),
                new Attribute("b", new SQLInt())
            )
        );

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(
                    new AliasableSelectItem(new ColumnReference("a")),
                    new AliasableSelectItem(new ColumnReference("b"))),
                new TableReference(myTable),
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference("a"),
                    new SQLPrimitiveInteger(1))
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    //TODO: rewrite this tests
//    @Test
//    public void parseSelectWithJoinClause() {
//        // Object parsed from input string
//        String selectWithJoin = "SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON (A.fk = B.pk) WHERE B.attr3 = 1.1;";
//        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(selectWithJoin);
//        parser.parse();
//        SQLObjectSchema schema = parser.getSQLObjectSchema();
//
//        // Object built directly in java
//        Query expectedSelect = new TableExpression(
//            List.of(
//                new AliasableSelectItem(new ColumnReference(new FullTableName("A"),"attr1")),
//                new AliasableSelectItem(new ColumnReference(new FullTableName("B"),"attr2"))
//            ), new OnJoin(OnJoin.JoinOperator.INNER,
//            new TableReference(new FullTableName("sameSchema", "A")),
//            new TableReference(new FullTableName("sameSchema", "B")),
//            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
//                new ColumnReference( new FullTableName("A"),"fk"),
//                new ColumnReference( new FullTableName("B"),"pk"))),
//            new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
//                new ColumnReference( new FullTableName("B"),"attr3"),
//                new SQLPrimitiveFloat(1.1f)),
//            true
//        );
//
//        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
//    }
//
//    //SELECT WITH RECURSIVE SELECT
//    @Test
//    public void parseSelectWithRecursiveSelectAndFrom() {
//        // Object parsed from input string
//        String basicSelect = "SELECT b AS money, (SELECT c FROM otherTable) FROM (SELECT a, b FROM myTable) WHERE a = 1;";
//        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicSelect);
//        parser.parse();
//        SQLObjectSchema schema = parser.getSQLObjectSchema();
//
//        // Object built directly in java
//        Query expectedSelect = new TableExpression(
//            List.of(
//                new AliasableSelectItem(new ColumnReference("b"), "money"),
//                new AliasableSelectItem(
//                    new TableExpression(
//                        List.of(new AliasableSelectItem(new ColumnReference("c"))),
//                        new TableReference(new FullTableName("otherTable")),null))),
//            new TableExpression(
//                List.of(
//                    new AliasableSelectItem(new ColumnReference("a")),
//                    new AliasableSelectItem(new ColumnReference("b"))),
//                new TableReference(new FullTableName("myTable")),
//                null),
//            new ComparisonPredicate(
//                ComparisonPredicate.ComparisonOperator.EQ,
//                new ColumnReference(null, "a"),
//                new SQLPrimitiveInteger(1)),
//            true
//        );
//
//        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
//    }
//
//    //MULTIPLE JOINS (check priority)
//    @Test
//    public void parseSelectWithMultipleJoinClausesOfPriority() {
//        // Object parsed from input string
//        String selectWithJoins = "SELECT * FROM A, B INNER JOIN C ON (B.fk = C.pk), (D CROSS JOIN E);";
//        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(selectWithJoins);
//        parser.parse();
//        SQLObjectSchema schema = parser.getSQLObjectSchema();
//
//        // Object built directly in java
//        Query expectedSelect = new TableExpression(
//            List.of(new Asterisk()),
//            new CrossJoin(
//                new CrossJoin(
//                    new TableReference(new FullTableName("A")),
//                    new OnJoin(
//                        OnJoin.JoinOperator.INNER,
//                        new TableReference(new FullTableName("B")),
//                        new TableReference(new FullTableName( "C")),
//                        new ComparisonPredicate(
//                            ComparisonPredicate.ComparisonOperator.EQ,
//                            new ColumnReference( new FullTableName("B"),"fk"),
//                            new ColumnReference( new FullTableName("C"),"pk")
//                        )
//                    )
//                ),
//                new CrossJoin(
//                    new TableReference(new FullTableName("D")),
//                    new TableReference(new FullTableName("E"))
//                )
//            ),
//            null,
//            true
//        );
//
//        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
//    }
//
//    //PREDICATES (not and,...)
//    @Test
//    public void parseSelectStatementWithComplexPredicate() {
//        // Object parsed from input string
//        String basicSelect = "SELECT *, * FROM myTable WHERE 1 = 0 AND ('SQLCommonSense' = '' AND NOT NOT (NOT 0 = 0));";
//        SQLObjectSchemaParser parser = new SQLObjectSchemaParser(basicSelect);
//        parser.parse();
//        SQLObjectSchema schema = parser.getSQLObjectSchema();
//
//        // Object built directly in java
//        Query expectedSelect = new TableExpression(
//            List.of(new Asterisk(), new Asterisk()),
//            new TableReference(new FullTableName("myTable")),
//            new PredicateOperation(
//                PredicateOperation.PredicateOperator.AND,
//                new ComparisonPredicate(
//                    ComparisonPredicate.ComparisonOperator.EQ,
//                    new SQLPrimitiveInteger(1),
//                    new SQLPrimitiveInteger(0)
//                ),
//                new PredicateOperation(
//                    PredicateOperation.PredicateOperator.AND,
//                    new ComparisonPredicate(
//                        ComparisonPredicate.ComparisonOperator.EQ,
//                        new SQLPrimitiveString("SQLCommonSense"),
//                        new SQLPrimitiveString("")
//                    ),
//                    new NotOperation(new NotOperation(new NotOperation(
//                        new ComparisonPredicate(
//                            ComparisonPredicate.ComparisonOperator.EQ,
//                            new SQLPrimitiveInteger(0),
//                            new SQLPrimitiveInteger(0)
//                        )
//                    )))
//                )
//            ),
//            true
//        );
//
//        assertThat("Parsed query does not equal expected query", schema.getSelects().get(0).equals(expectedSelect));
//    }
}
