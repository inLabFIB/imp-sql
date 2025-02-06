package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ExistsPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.NotOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.PredicateOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.CrossJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

class AssertionsStandardSQLParserTest {


    @Test
    void parseSimpleCreateAssertionStatement() {
        // Object parsed from input string
        String basicAssertion = "CREATE ASSERTION assertionName CHECK ( NOT EXISTS ( SELECT 1))";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(basicAssertion);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Assertion expectedAssertion = new Assertion(
            "assertionName",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1))),
                    null, null
                )
            ))
        );

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));
    }

    @Test
    void parseAssertionWithAliases() {
        StandardSQLParser parser = new StandardSQLParser();

        String tableA = "CREATE TABLE a (b int, c int)";
        parser.parse(tableA);

        // Object parsed from input string
        String assertion = """
        CREATE ASSERTION assertionName CHECK ( NOT EXISTS (
            SELECT a.b, d.e FROM a, (SELECT a.c as e FROM a) as d
        ))""";
        parser.parse(assertion);

        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Assertion expectedAssertion = new Assertion(
            "assertionName",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(
                        new AliasableSelectItem(new ColumnReference("a", "b")),
                        new AliasableSelectItem(new ColumnReference("d", "e"))),
                    new CrossJoin(
                        new TableReference(schema.getTables().get(0)),
                        new TableExpression(
                            List.of(new AliasableSelectItem(new ColumnReference("a", "c"), "e")),
                            new TableReference(schema.getTables().get(0)),
                            null,
                            "d"
                        )
                    ),
                    null
                )
            ))
        );

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));
    }

    @Test
    void parseCreateAssertionWithOrOperation() {
        // Object parsed from input string
        String basicAssertion = "CREATE ASSERTION assertionName CHECK ( 1=1 OR 1<>1 )";
        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(basicAssertion);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        // Object built directly in java
        Assertion expectedAssertion = new Assertion(
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

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));
    }
}
