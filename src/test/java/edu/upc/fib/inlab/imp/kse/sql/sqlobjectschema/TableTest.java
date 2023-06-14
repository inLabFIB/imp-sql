package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.RepeatedAttributeNamesInSameTable;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLInt;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableTest {

    @Test
    public void tableWithRepeatedColumnNamesThrowsException() {
        assertThrows(RepeatedAttributeNamesInSameTable.class, () -> new Table(
            "tableName",
            List.of(new Attribute("col1", new SQLInt()), new Attribute("col1", new SQLInt()))));
    }
}
