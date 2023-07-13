package edu.upc.fib.inlab.imp.kse.sql.sql_server.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.Table;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.TableConstraint;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLFunction;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

public class SQLServerFetcherIT {

    public static final String TEST_DB = "test_db";
    private static String serverName;

    @BeforeAll
    static void beforeAll() {
        serverName = isNull(System.getenv("SQL_DB_HOST"))? "localhost": System.getenv("SQL_DB_HOST");
    }

    @Test
    public void fetchTestTables() {
        SQLServerFetcher fetcher = new SQLServerFetcher(serverName, 1433, TEST_DB, "SA", "PasswordO1.");

        SQLObjectSchema schema = fetcher.fetch(TEST_DB, List.of("test_schema", "ref_test_schema"));

        assertThat(schema.getTables()).hasSize(3);
    }

    @Test
    public void fetchTestTableConstraints() {
        SQLServerFetcher fetcher = new SQLServerFetcher(serverName, 1433, TEST_DB, "SA", "PasswordO1.");

        SQLObjectSchema schema = fetcher.fetch(TEST_DB, List.of("test_schema", "ref_test_schema"));

        Table testedTable = schema.getTables().stream().filter(s -> s.getTableName().equals("test")).findFirst().orElseThrow();

        List<TableConstraint> tableConstraints = testedTable.getTableConstraints();
        assertThat(tableConstraints).hasSize(7);
        assertThat(tableConstraints).extracting(TableConstraint::getName).contains(
            "test_pk", "test_fk_1", "test_fk_2", "test_fk_self", "test_ck", "test_u" // There is a column-check with a random name
        );
    }

    @ParameterizedTest(name = "{index} - Attribute type {0}")
    @MethodSource("attributesProvider")
    public void fetchTestTableAttribute(Attribute attribute) {
        SQLServerFetcher fetcher = new SQLServerFetcher(serverName, 1433, TEST_DB, "SA", "PasswordO1.");

        SQLObjectSchema schema = fetcher.fetch(TEST_DB, List.of("test_schema", "ref_test_schema"));

        Table testedTable = schema.getTables().stream().filter(s -> s.getTableName().equals("test")).findFirst().orElseThrow();
        assertThat(testedTable.getAttributes()).contains(attribute);
    }

    public static Stream<Arguments> attributesProvider() {
        return Stream.of(
            Arguments.of(named("SQLBit", new Attribute("btAttr", new SQLBit()))),
            Arguments.of(named("SQLChar with default NEWID()", new Attribute("chAttr", new SQLCharacter(255), false, new SQLFunction("newid")))), // Not null since it is a PK
            Arguments.of(named("SQLDateTime", new Attribute("dtAttr", new SQLDateTime(7)))),
            Arguments.of(named("SQLFloat", new Attribute("dpAttr", new SQLFloat(53)))), // SQLServer converts double precision to float(53)
            Arguments.of(named("SQLReal", new Attribute("flAttr", new SQLReal()))),  // SQLServer converts floats with precision to reals
            Arguments.of(named("SQLInt with default value", new Attribute("itAttr", new SQLInteger(), false, new SQLPrimitiveInteger(1)))), // Not null since it is a PK
            Arguments.of(named("SQLReal", new Attribute("rlAttr", new SQLReal(), false))),
            Arguments.of(named("SQLSmallint", new Attribute("siAttr", new SQLSmallint()))),
            Arguments.of(named("SQLVarchar", new Attribute("vcAttr", new SQLVarchar(64)))),
            Arguments.of(named("SQLReal", new Attribute("selfRl", new SQLReal())))
        );
    }
}
