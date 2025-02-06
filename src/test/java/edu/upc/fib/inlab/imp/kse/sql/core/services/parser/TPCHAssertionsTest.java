package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.Table;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ExistsPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.NotOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.OnJoin;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

class TPCHAssertionsTest {

    @Test
    void checkSupplierNotCustomerAssertion() {
        String dummyTables = """
            CREATE TABLE LINEITEM (L_ORDERKEY int, L_SUPPKEY int);
            CREATE TABLE SUPPLIER (S_SUPPKEY int, S_NAME varchar(10));
            CREATE TABLE ORDERS (O_ORDERKEY int, O_CUSTKEY int);
            CREATE TABLE CUSTOMER (C_CUSTKEY int, C_NAME varchar(10));
            """;

        String createAssertionStatement = """
            CREATE ASSERTION supplierNotCustomer CHECK ( NOT EXISTS (
               SELECT *
               FROM LINEITEM AS l JOIN SUPPLIER AS s ON (l.L_SUPPKEY = s.S_SUPPKEY)
                                   JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
                                   JOIN CUSTOMER AS c ON (o.O_CUSTKEY = c.C_CUSTKEY)
               WHERE s.S_NAME = c.C_NAME
            ));
            """;

        StandardSQLParser parser = new StandardSQLParser();
        parser.parse(dummyTables);
        parser.parse(createAssertionStatement);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table lineitemTable = schema.getTable("LINEITEM",null);
        Table supplierTable = schema.getTable("SUPPLIER",null);
        Table ordersTable = schema.getTable("ORDERS",null);
        Table customerTable = schema.getTable("CUSTOMER",null);

        Assertion expectedAssertion = new Assertion(
            "supplierNotCustomer",
            new NotOperation(new ExistsPredicate(
                new TableExpression(
                    List.of(new Asterisk()),
                    new OnJoin(
                        OnJoin.JoinOperator.INNER,
                        new OnJoin(
                            OnJoin.JoinOperator.INNER,
                            new OnJoin(
                                OnJoin.JoinOperator.INNER,
                                new TableReference(lineitemTable, "l"),
                                new TableReference(supplierTable, "s"),
                                new ComparisonPredicate(
                                    ComparisonPredicate.ComparisonOperator.EQ,
                                    new ColumnReference("l","L_SUPPKEY"),
                                    new ColumnReference("s","S_SUPPKEY")

                                )
                            ),
                            new TableReference(ordersTable, "o"),
                            new ComparisonPredicate(
                                ComparisonPredicate.ComparisonOperator.EQ,
                                new ColumnReference("l","L_ORDERKEY"),
                                new ColumnReference("o","O_ORDERKEY")
                            )
                        ),
                        new TableReference(customerTable, "c"),
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new ColumnReference("o","O_CUSTKEY"),
                            new ColumnReference("c","C_CUSTKEY")
                        )
                    ),
                    new ComparisonPredicate(
                        ComparisonPredicate.ComparisonOperator.EQ,
                        new ColumnReference("s","S_NAME"),
                        new ColumnReference("c","C_NAME"))
                )
            ))
        );

        Assertions.assertThat(schema.getAssertions().get(0))
            .usingRecursiveComparison()
            .isEqualTo(expectedAssertion);

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getAssertions().get(0).equals(expectedAssertion));



    }
}
