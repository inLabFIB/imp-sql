package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.SQLObjectSchemaParser;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;

class AssertionTest {

    @Test
    public void shouldGenerateViewFromSimpleAssertion() {
        String schema = """
            CREATE TABLE LINEITEM (L_ORDERKEY int, L_COMMITDATE int);
            CREATE TABLE ORDERS (O_ORDERKEY int, O_ORDERDATE int);
            """;

        String assertionString = """
            CREATE ASSERTION db.schema1.correctDates CHECK ( NOT EXISTS (
                SELECT *
                FROM LINEITEM AS l JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
                WHERE l.L_COMMITDATE < o.O_ORDERDATE
            ));
            """;

        String viewString = """
            CREATE VIEW schema1.correctDates AS SELECT *
                FROM LINEITEM AS l JOIN ORDERS AS o ON (l.L_ORDERKEY = o.O_ORDERKEY)
                WHERE l.L_COMMITDATE < o.O_ORDERDATE
            ;
            """;

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(schema);
        parser.parse(assertionString);
        parser.parse(viewString);

        Assertion assertion = parser.getSQLObjectSchema().getAssertions().get(0);
        View expectedView = parser.getSQLObjectSchema().getViews().get(0);
        View obtainedView = assertion.getEquivalentViolationDetectionView();

        assertThat("Obtained view is not equal to expected views.",
            obtainedView.equals(expectedView));
    }
}
