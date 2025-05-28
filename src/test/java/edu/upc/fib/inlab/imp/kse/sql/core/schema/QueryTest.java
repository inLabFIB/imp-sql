package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.TableExpression;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QueryTest {

    @Test
    void queryWithoutSelectItemsShouldThrowException() {
        assertThatThrownBy(() -> new TableExpression(List.of()), "Query with no select items should throw an exception", IMPSqlException.class);
    }
}
