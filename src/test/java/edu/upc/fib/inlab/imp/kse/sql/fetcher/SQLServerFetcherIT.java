package edu.upc.fib.inlab.imp.kse.sql.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.services.fetcher.SQLObjectSchemaFetcher;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.TableConstraint;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SQLServerFetcherIT {

    @Test
    public void fetchTestTables() {
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "test_db",
            List.of("test_schema", "ref_test_schema"),
            "SA",
            "PasswordO1.",
            SQLObjectSchemaFetcher.DBType.SQLServer
        );

        fetcher.fetch();

        SQLObjectSchema schema = fetcher.getSQLObjectSchema();

        assertThat(schema.getTables(), hasSize(3));

        Table testedTable = schema.getTables().stream().filter(s -> s.getTableName().equals("test")).findFirst().orElseThrow();
        assertThat(testedTable.getAttributes(), containsInAnyOrder(List.of(
            new Attribute("btAttr", new SQLBit()),
            new Attribute("chAttr", new SQLChar(8), false), // Not null since it is a PK
            new Attribute("dtAttr", new SQLDate(7)),
            new Attribute("dpAttr", new SQLFloat(53)), // SQLServer converts double precision to float(53)
            new Attribute("flAttr", new SQLReal()),  // SQLServer converts floats with precision to reals
            new Attribute("itAttr", new SQLInt(), false, new SQLPrimitiveInteger(1)), // Not null since it is a PK
            new Attribute("rlAttr", new SQLReal(), false),
            new Attribute("siAttr", new SQLSmallint()),
            new Attribute("vcAttr", new SQLVarchar(64)),
            new Attribute("selfRl", new SQLReal())
        ).toArray()));

        assertThat(testedTable.getTableConstraints(), hasSize(7));
        assertThat(testedTable.getTableConstraints().stream().map(TableConstraint::getName).toList(), hasItems(
            "test_pk", "test_fk_1", "test_fk_2", "test_fk_self", "test_ck", "test_u" // There is a column-check with a random name
        ));
    }
}
