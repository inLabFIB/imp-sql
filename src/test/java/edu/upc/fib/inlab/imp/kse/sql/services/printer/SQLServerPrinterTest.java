package edu.upc.fib.inlab.imp.kse.sql.services.printer;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.PredicateOperation;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveFloat;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveString;
import edu.upc.fib.inlab.imp.kse.sql.utils.SchemasProvider;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

//TODO: separate in different test  classes?
class SQLServerPrinterTest {

    /** TABLE **/
    @Test
    public void printTableWithAttrTypes() {
        // Object built directly in Java
        Table table = new Table(
            "tableName",
            new SchemaReference("schemaName"),
            List.of(
                new Attribute("btAttr", new SQLBit()),
                new Attribute("chAttr", new SQLChar(8)),
                new Attribute("dtAttr", new SQLDate(7)),
                new Attribute("dpAttr", new SQLDoublePrecision()),
                new Attribute("flAttr", new SQLFloat(16)),
                new Attribute("itAttr", new SQLInt(), new SQLPrimitiveInteger(1)),
                new Attribute("rlAttr", new SQLReal(), false),
                new Attribute("siAttr", new SQLSmallint()),
                new Attribute("vcAttr", new SQLVarchar(64))
            )
        );

        String expectedTable = "CREATE TABLE schemaName.tableName ( btAttr BIT, chAttr CHAR(8), dtAttr DATETIME2(7), " +
            "dpAttr DOUBLE PRECISION, flAttr FLOAT(16), itAttr INT DEFAULT 1, rlAttr REAL NOT NULL, siAttr SMALLINT, " +
            "vcAttr VARCHAR(64) );";
        assertThat(table.visit(new SQLServerPrinter()), is(expectedTable));
    }

    @Test
    public void printTableWithConstraints() {
        Attribute attrA = new Attribute("attrA", new SQLInt());
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
        MatcherAssert.assertThat(assertion.visit(new SQLServerPrinter()), is(expectedAssertion));
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
        MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
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
        MatcherAssert.assertThat(view.visit(new SQLServerPrinter()), is(expectedView));
    }

    /** SELECTS **/
    /* All prints contain parenthesis and no ending ';' because they are interpreted as subqueries. */

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
        MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
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
        MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
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
        MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedAssertion));
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
        MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
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
        MatcherAssert.assertThat(select.visit(new SQLServerPrinter()), is(expectedSelect));
    }
}
