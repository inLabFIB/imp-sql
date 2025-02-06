package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.Table;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.View;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.NotOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.PredicateOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.CrossJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.OnJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveFloat;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveString;
import edu.upc.fib.inlab.imp.kse.sql.core.utils.SchemasProvider;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.upc.fib.inlab.imp.kse.sql.core.utils.SchemasProvider.getMyTableSchemaStatements;
import static edu.upc.fib.inlab.imp.kse.sql.core.utils.SchemasProvider.getMyTableSchemaTables;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;

class ViewsStandardSQLParserTest {

    @Nested
    class ParsingViewsAfterParsingTablesTests {

        @Test
        void parseTrivialCrateViewStatement() {
            // Object parsed from input string
            String basicView = "CREATE TABLE tableA ( c1 INT, c2 INT ); CREATE VIEW view1 AS ( SELECT 1 );";
            StandardSQLParser parser = new StandardSQLParser();
            parser.parse(basicView);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            assertThat(schema).isNotNull();
        }

    }

    @Test
    void parseTrivialCrateViewStatementWithColumnNames() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName (col1) AS SELECT 1;";
        StandardSQLParser parser = new StandardSQLParser();
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
    void parseTrivialCrateViewStatement() {
        // Object parsed from input string
        String basicView = "CREATE VIEW viewName AS SELECT 1;";
        StandardSQLParser parser = new StandardSQLParser();
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

    @Test
    void parseCreateViewStatementWithSimpleSelect() {
        // Object parsed from input string
        String basicSelect = "CREATE VIEW viewName AS SELECT a, b FROM myTable WHERE a = 1;";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(getMyTableSchemaStatements());
        parser.parse(basicSelect);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table myTable = getMyTableSchemaTables().get(0);

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
    void parseSelectWithJoinClause() {
        // Object parsed from input string
        String selectWithJoin = "CREATE VIEW viewName AS SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON (A.fk = B.pk) WHERE B.attr3 = 1.1;";
        StandardSQLParser parser = new StandardSQLParser();
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
    void parseSelectWithRecursiveSelectAndFrom() {
        // Object parsed from input string
        String basicSelect = "CREATE VIEW viewName AS SELECT b AS money, (SELECT c FROM otherTable) FROM (SELECT a, b FROM myTable) WHERE a = 1;";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(getMyTableSchemaStatements());
        parser.parse(basicSelect);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = getMyTableSchemaTables();

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
    void parseSelectWithMultipleJoinClausesOfPriority() {
        // Object parsed from input string
        String selectWithJoins = "CREATE VIEW viewName AS SELECT * FROM A, B INNER JOIN C ON (B.B_pk = C.C_pk), (D CROSS JOIN E);";
        StandardSQLParser parser = new StandardSQLParser();
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

    @Test
    void parseSelectWithMultipleCrossJoins() {
        // Object parsed from input string
        String selectWithJoins = """
            CREATE VIEW viewName AS
                SELECT *
                FROM A
                CROSS JOIN B
                CROSS JOIN C;
            """;

        StandardSQLParser parser = new StandardSQLParser();
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
                        new TableReference(expectedSchemaTables.get(1))
                    ),
                    new TableReference(expectedSchemaTables.get(2))
                ),
                null
            ));

        AssertionsForClassTypes.assertThat(schema.getViews().get(0))
            .usingRecursiveComparison()
            .isEqualTo(expectedView);
    }

    //PREDICATES (not and,...)
    @Test
    void parseSelectStatementWithComplexPredicate() {
        // Object parsed from input string
        String selectStatement = "CREATE VIEW viewName AS SELECT *, * FROM myTable WHERE 1 = 0 AND ('SQLCommonSense' = '' AND NOT NOT (NOT 0 = 0));";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(getMyTableSchemaStatements());
        parser.parse(selectStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = getMyTableSchemaTables();

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

    @Test
    void parseSelectStatementWithAliasedColumns() {
        String selectStatement = "CREATE VIEW viewName AS SELECT sub.q as d FROM (SELECT myTable.a as q FROM myTable) as sub;";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(getMyTableSchemaStatements());
        parser.parse(selectStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        List<Table> expectedSchemaTables = getMyTableSchemaTables();

        // Object built directly in java
        View expectedView = new View(
            "viewName",
            new TableExpression(
                List.of(new AliasableSelectItem(
                    new ColumnReference("sub", "q"),
                    "d"
                )),
                new TableExpression(
                    List.of(new AliasableSelectItem(
                        new ColumnReference("myTable", "a"),
                        "q"
                    )),
                    new TableReference(expectedSchemaTables.get(0)),
                    null,
                    "sub"
                ),
                null
            )
        );

        assertThat("Parsed view does not equal expected view", schema.getViews().get(0).equals(expectedView));
    }

}
