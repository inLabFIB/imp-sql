package edu.upc.fib.inlab.imp.kse.sql;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

public class EqualAndHashCodeTest {

    @Test
    public void identicalTablesShouldBeEqualAndHaveTheSameHashCode() {
        Table t1 = new Table("tableA", List.of(
            new Attribute("col1", new SQLInt()),
            new Attribute("col2", new SQLInt()),
            new Attribute("col3", new SQLInt())
        ));

        Table t2 = new Table("tableA", List.of(
            new Attribute("col1", new SQLInt()),
            new Attribute("col2", new SQLInt()),
            new Attribute("col3", new SQLInt())
        ));

        assertThat("Equal method doesn't work as expected!", t1.equals(t2));
        assertThat("HashCode method doesn't work as expected!", t1.hashCode() == t2.hashCode());
    }
}
