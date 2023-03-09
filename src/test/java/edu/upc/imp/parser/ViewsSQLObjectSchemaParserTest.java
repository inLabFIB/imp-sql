package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.View;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.imp.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.imp.sqlobjectschema.boolean_expressions.PredicateOperation;
import edu.upc.imp.sqlobjectschema.relational_expressions.CrossJoin;
import edu.upc.imp.sqlobjectschema.relational_expressions.OnJoin;
import edu.upc.imp.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.imp.sqlobjectschema.relational_expressions.TableReference;
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

public class ViewsSQLObjectSchemaParserTest {

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
        parser.parse(SchemasProvider.getMyTableSchemaStatements());
        parser.parse(basicSelect);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table myTable = SchemasProvider.getMyTableSchemaTables().get(0);

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


    @Test
    public void parseSelectWithJoinClause() {
        // Object parsed from input string
        String selectWithJoin = "CREATE VIEW viewName AS SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON (A.fk = B.pk) WHERE B.attr3 = 1.1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(SchemasProvider.getABSchemaStatements());
        parser.parse(selectWithJoin);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = SchemasProvider.getABSchemaTables();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(
                    new AliasableSelectItem(new ColumnReference("A","attr1")),
                    new AliasableSelectItem(new ColumnReference("B","attr2"))
                ), new OnJoin(OnJoin.JoinOperator.INNER,
                new TableReference(expectedSchemaTables.get(0)),
                new TableReference(expectedSchemaTables.get(1)),
                new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference("A","fk"),
                    new ColumnReference("B","pk"))),
                new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference("B","attr3"),
                    new SQLPrimitiveFloat(1.1f))
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    //SELECT WITH RECURSIVE SELECT
    @Test
    public void parseSelectWithRecursiveSelectAndFrom() {
        // Object parsed from input string
        String basicSelect = "CREATE VIEW viewName AS SELECT b AS money, (SELECT c FROM otherTable) FROM (SELECT a, b FROM myTable) WHERE a = 1;";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(SchemasProvider.getMyTableSchemaStatements());
        parser.parse(basicSelect);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = SchemasProvider.getMyTableSchemaTables();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(
                    new AliasableSelectItem(new ColumnReference("b"), "money"),
                    new AliasableSelectItem(
                        new TableExpression(
                            List.of(new AliasableSelectItem(new ColumnReference("c"))),
                            new TableReference(expectedSchemaTables.get(1)),null))),
                new TableExpression(
                    List.of(
                        new AliasableSelectItem(new ColumnReference("a")),
                        new AliasableSelectItem(new ColumnReference("b"))),
                    new TableReference(expectedSchemaTables.get(0)),
                    null),
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference(null, "a"),
                    new SQLPrimitiveInteger(1))
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    //MULTIPLE JOINS (check priority)
    @Test
    public void parseSelectWithMultipleJoinClausesOfPriority() {
        // Object parsed from input string
        String selectWithJoins = "CREATE VIEW viewName AS SELECT * FROM A, B INNER JOIN C ON (B.B_pk = C.C_pk), (D CROSS JOIN E);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(SchemasProvider.getJoinsSchemaStatements());
        parser.parse(selectWithJoins);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = SchemasProvider.getJoinsSchemaTables();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(new Asterisk()),
                new CrossJoin(
                    new CrossJoin(
                        new TableReference(expectedSchemaTables.get(0)),
                        new OnJoin(
                            OnJoin.JoinOperator.INNER,
                            new TableReference(expectedSchemaTables.get(1)),
                            new TableReference(expectedSchemaTables.get(2)),
                            new ComparisonPredicate(
                                ComparisonPredicate.ComparisonOperator.EQ,
                                new ColumnReference("B","B_pk"),
                                new ColumnReference("C","C_pk")
                            )
                        )
                    ),
                    new CrossJoin(
                        new TableReference(expectedSchemaTables.get(3)),
                        new TableReference(expectedSchemaTables.get(4))
                    )
                ),
                null
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

    //PREDICATES (not and,...)
    @Test
    public void parseSelectStatementWithComplexPredicate() {
        // Object parsed from input string
        String selectStatement = "CREATE VIEW viewName AS SELECT *, * FROM myTable WHERE 1 = 0 AND ('SQLCommonSense' = '' AND NOT NOT (NOT 0 = 0));";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(SchemasProvider.getMyTableSchemaStatements());
        parser.parse(selectStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = SchemasProvider.getMyTableSchemaTables();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(new Asterisk(), new Asterisk()),
                new TableReference(expectedSchemaTables.get(0)),
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
            ));

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }
}
