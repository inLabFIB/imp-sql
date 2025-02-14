package edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.utils.SchemasProvider;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.SetOperation.SetOperator.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class SQLServerPrinterTest {

    @Nested
    class TableTests {
        @Test
        void printTableWithAttrTypes() {
            // Object built directly in Java
            Table table = new Table(
                "tableName",
                new SchemaReference("schemaName"),
                List.of(
                    new Attribute("btAttr", new SQLBit()),
                    new Attribute("chAttr", new SQLCharacter(8), new SQLFunction("myFunction")),
                    new Attribute("dtAttr", new SQLDateTime(7)),
                    new Attribute("dpAttr", new SQLDoublePrecision()),
                    new Attribute("flAttr", new SQLFloat(16)),
                    new Attribute("itAttr", new SQLInteger(), new SQLPrimitiveInteger(1)),
                    new Attribute("rlAttr", new SQLReal(), false),
                    new Attribute("siAttr", new SQLSmallint()),
                    new Attribute("vcAttr", new SQLVarchar(64))
                )
            );

            String expectedTable = "CREATE TABLE schemaName.tableName ( btAttr BINARY, chAttr CHAR(8) DEFAULT myFunction(), dtAttr DATETIME(7), " +
                "dpAttr DOUBLE PRECISION, flAttr FLOAT(16), itAttr INT DEFAULT 1, rlAttr REAL NOT NULL, siAttr SMALLINT, " +
                "vcAttr VARCHAR(64) );";
            assertThat(table.visit(new SQLServerPrinter()), is(expectedTable));
        }

        @Test
        void printTableWithConstraints() {
            Attribute attrA = new Attribute("attrA", new SQLInteger());
            Attribute attrB = new Attribute("attrB", new SQLVarchar(64));

            Attribute refPk = new Attribute("pk", new SQLVarchar(64));
            Table refTable = new Table("refTable", null, List.of(refPk));
            // Object built directly in Java
            Table table = new Table(
                "tableName",
                new SchemaReference("schemaName"),
                List.of(attrA, attrB),
                List.of(
                    new Check("CK", new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new ColumnReference("attrA"),
                        new SQLPrimitiveInteger(1)
                    ))
                ),
                List.of(new Unique("U", List.of(attrB))),
                List.of(
                    new PrimaryKey("PK", List.of(attrA, attrB))
                ),
                List.of(
                    new ForeignKey(
                        "FK",
                        List.of(attrB),
                        refTable,
                        List.of(refPk)
                    )
                )
            );

            String expectedTable = "CREATE TABLE schemaName.tableName ( attrA INT, attrB VARCHAR(64), " +
                "CONSTRAINT CK CHECK (attrA = 1), CONSTRAINT U UNIQUE (attrB), CONSTRAINT PK PRIMARY KEY (attrA, attrB), " +
                "CONSTRAINT FK FOREIGN KEY (attrB) REFERENCES refTable (pk) );";
            assertThat(table.visit(new SQLServerPrinter()), is(expectedTable));
        }

        @Test
        void attributeTypesArePrintedAsExpected() {
            Table table = new Table("table", List.of(
                new Attribute("col1", new SQLBit()),
                new Attribute("col2", new SQLBit(2)),
                new Attribute("col3", new SQLBit(4), false),
                new Attribute("col4", new SQLCharacter()),
                new Attribute("col5", new SQLCharacter(2)),
                new Attribute("col6", new SQLFloat(4))
            ));

            String expectedSelect = "CREATE TABLE table ( col1 BINARY, col2 BINARY(2), col3 BINARY(4) NOT NULL, col4 CHAR," +
                " col5 CHAR(2), col6 FLOAT(4) );";
            MatcherAssert.assertThat(table.visit(new SQLServerPrinter()), is(expectedSelect));
        }
    }

    @Nested
    class AssertionTests {
        @Test
        void printSimpleCreateAssertionStatement() {
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
            MatcherAssert.assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
        }
    }

    @Nested
    class ViewTests {
        @Test
        void printCrateViewStatementWithColumnNames() {
            // Object built directly in java
            View view = new View(
                "viewName", null, List.of("col1"),
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                ));

            String expectedView = "CREATE VIEW viewName ( col1 ) AS ( SELECT 1 );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }

        @Test
        void printCrateViewStatement() {
            // Object built directly in java
            View view = new View(
                "viewName",
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                ));

            String expectedView = "CREATE VIEW viewName AS ( SELECT 1 );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }

        @Test
        void printViewWithSchema() {
            View view = new View(
                "viewName",
                new SchemaReference("db", "schema"),
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                ));

            String expectedView = "CREATE VIEW db.schema.viewName AS ( SELECT 1 );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }

    }

    /**
     * All prints contain parenthesis and no ending ';' because they are interpreted as subqueries.
     */
    @Nested
    class SelectTests {

        @Nested
        class SimpleSelectTests {
            @Test
            void printSelectStatement() {
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
                MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
            }

            @Test
            void parseSelectStatementWithComplexPredicate() {
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
                MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
            }
        }

        @Nested
        class JoinTests {
            @Test
            void printSelectWithJoinClause() {
                // Object built directly in java
                Query select = new TableExpression(
                    List.of(
                        new AliasableSelectItem(new ColumnReference("A", "attr1")),
                        new AliasableSelectItem(new ColumnReference("B", "attr2"))
                    ), new OnJoin(OnJoin.JoinOperator.INNER,
                                  new TableReference(SchemasProvider.getABSchemaTables().get(0)),
                                  new TableReference(SchemasProvider.getABSchemaTables().get(1)),
                                  new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                                                          new ColumnReference("A", "fk"),
                                                          new ColumnReference("B", "pk"))),
                    new ComparisonPredicate(ComparisonPredicate.ComparisonOperator.EQ,
                                            new ColumnReference("B", "attr3"),
                                            new SQLPrimitiveFloat(1.1f))
                );

                String expectedSelect = "( SELECT A.attr1, B.attr2 FROM sameSchema.A INNER JOIN sameSchema.B ON ( A.fk = B.pk ) WHERE B.attr3 = 1.1 )";
                MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
            }

            @Test
            void parseSelectWithMultipleJoinClausesOfPriority() {
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
                                    new ColumnReference("B", "B_fk"),
                                    new ColumnReference("C", "C_pk")
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
                MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
            }
        }

        @Nested
        class RecursiveSelectTests {
            @Test
            void printSelectWithRecursiveSelectAndFrom() {
                // Object built directly in java
                Query select = new TableExpression(
                    List.of(
                        new AliasableSelectItem(new ColumnReference("b"), "money"),
                        new AliasableSelectItem(
                            new TableExpression(
                                List.of(new AliasableSelectItem(new ColumnReference("c"))),
                                new TableReference(SchemasProvider.getMyTableSchemaTables().get(1)), null))),
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
                MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
            }
        }

        @Nested
        class SetOperationTests {

            private static Stream<Arguments> providesSetOperators() {
                return Stream.of(
                    Arguments.of(UNION, false, "UNION"),
                    Arguments.of(UNION, true, "UNION ALL"),
                    Arguments.of(EXCEPT, false, "EXCEPT"),
                    Arguments.of(INTERSECT, false, "INTERSECT")
                );
            }

            @ParameterizedTest
            @MethodSource("providesSetOperators")
            void printSetOperators(SetOperation.SetOperator operator, boolean repeatedValues, String setOperator) {
                // Object built directly in java
                Query union = new SetOperation(operator, repeatedValues,
                                               new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1)))),
                                               new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(2))))
                );
                String expectedUnion = "( ( SELECT 1 ) " + setOperator + " ( SELECT 2 ) )";
                MatcherAssert.assertThat(union.visit(new SQLServerPrinter()), is(expectedUnion));
            }

            @Test
            void printMultipleUnions() {
                Query query1 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))));
                Query query2 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(2))));
                Query query3 = new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(3))));
                // Object built directly in java
                Query union = new SetOperation(UNION, false,
                                               new SetOperation(EXCEPT, false, query1, query2),
                                               query3
                );
                String expectedUnion = "( ( ( SELECT 1 ) EXCEPT ( SELECT 2 ) ) UNION ( SELECT 3 ) )";
                MatcherAssert.assertThat(union.visit(new SQLServerPrinter()), is(expectedUnion));
            }

        }
    }

    @Nested
    class InPredicateTests {
        @Test
        void printInPredicate() {
            View view = new View(
                "viewName",
                new SchemaReference("db", "schema"),
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    new TableReference(new Table("t", List.of(new Attribute("at1", new SQLInteger())))),
                    new ValueListInPredicate(new SQLPrimitiveInteger(1), List.of(new SQLPrimitiveInteger(1), new SQLPrimitiveInteger(2)))
                ));

            String expectedView = "CREATE VIEW db.schema.viewName AS ( SELECT 1 FROM t WHERE 1 IN ( 1, 2 ) );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }

        @Test
        void printInPredicate2() {
            View view = new View(
                "viewName",
                new SchemaReference("db", "schema"),
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    new TableReference(new Table("t", List.of(new Attribute("at1", new SQLInteger())))),
                    new ValueListInPredicate(new ColumnReference("t", "at1"), List.of(new SQLPrimitiveInteger(1), new SQLPrimitiveInteger(2)))
                ));

            String expectedView = "CREATE VIEW db.schema.viewName AS ( SELECT 1 FROM t WHERE t.at1 IN ( 1, 2 ) );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }
    }

    @Nested
    class OrOperationTests {
        @Test
        void printAssertionWithOr() {
            Assertion assertion = new Assertion(
                "assertionName",
                new PredicateOperation(
                    PredicateOperation.PredicateOperator.OR,
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new SQLPrimitiveInteger(1),
                        new SQLPrimitiveInteger(1)
                    ),
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.NEQ,
                        new SQLPrimitiveInteger(1),
                        new SQLPrimitiveInteger(1)
                    )
                )
            );

            String expectedAssertion = "CREATE ASSERTION assertionName CHECK ( 1 = 1 OR 1 <> 1 );";
            MatcherAssert.assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
        }

        @Test
        void printOrInWhere() {
            View view = new View(
                "viewName",
                new SchemaReference("db", "schema"),
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    new TableReference(new Table("t", List.of(new Attribute("at1", new SQLInteger())))),
                    new PredicateOperation(
                        PredicateOperation.PredicateOperator.OR,
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new SQLPrimitiveInteger(1),
                            new SQLPrimitiveInteger(1)
                        ),
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new SQLPrimitiveInteger(1),
                            new SQLPrimitiveInteger(1)
                        )
                    )
                ));

            String expectedView = "CREATE VIEW db.schema.viewName AS ( SELECT 1 FROM t WHERE 1 = 1 OR 1 = 1 );";
            MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
        }
    }

    @Nested
    class UnionTests {
        @Test
        void unionOfSelects() {
            Assertion assertion = new Assertion(
                "assertionName",
                new NotOperation(new ExistsPredicate(
                    new SetOperation(
                        UNION,
                        true,
                        new TableExpression(
                            List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                            null, null
                        ), new TableExpression(
                        List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                        null, null
                    )
                    )
                ))
            );

            String expectedAssertion = "CREATE ASSERTION assertionName CHECK ( NOT ( EXISTS ( ( SELECT 1 ) UNION ALL ( SELECT 1 ) ) ) );";
            MatcherAssert.assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
        }
    }

}
