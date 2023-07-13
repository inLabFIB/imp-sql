package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.SQLInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.exceptions.RepeatedAttributeNamesInSameTable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class TableTest {

    @Test
    public void tableWithRepeatedColumnNamesThrowsException() {
        assertThrows(RepeatedAttributeNamesInSameTable.class, () -> new Table(
            "tableName",
            List.of(new Attribute("col1", new SQLInteger()), new Attribute("col1", new SQLInteger()))));
    }
}
