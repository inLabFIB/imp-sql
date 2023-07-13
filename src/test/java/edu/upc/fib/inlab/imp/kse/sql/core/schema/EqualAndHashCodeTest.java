package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.SQLInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class EqualAndHashCodeTest {

    @Test
    public void identicalTablesShouldBeEqualAndHaveTheSameHashCode() {
        Table t1 = new Table("tableA", List.of(
            new Attribute("col1", new SQLInteger()),
            new Attribute("col2", new SQLInteger()),
            new Attribute("col3", new SQLInteger())
        ));

        Table t2 = new Table("tableA", List.of(
            new Attribute("col1", new SQLInteger()),
            new Attribute("col2", new SQLInteger()),
            new Attribute("col3", new SQLInteger())
        ));

        assertThat("Equal method doesn't work as expected!", t1.equals(t2));
        assertThat("HashCode method doesn't work as expected!", t1.hashCode() == t2.hashCode());
    }
}
