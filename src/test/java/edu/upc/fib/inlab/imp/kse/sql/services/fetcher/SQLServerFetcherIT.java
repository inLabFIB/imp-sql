package edu.upc.fib.inlab.imp.kse.sql.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.TableConstraint;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLFunction;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Named.named;

public class SQLServerFetcherIT {

    @Test
    public void fetchTestTables() {
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "SA", "PasswordO1.", "test_db",
            List.of("test_schema", "ref_test_schema"),
            SQLObjectSchemaFetcher.DBType.SQLServer
        );

        fetcher.fetch();

        SQLObjectSchema schema = fetcher.getSQLObjectSchema();

        assertThat(schema.getTables()).hasSize(3);
    }

    @Test
    public void fetchTestTableConstraints() {
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "SA", "PasswordO1.", "test_db",
            List.of("test_schema", "ref_test_schema"),
            SQLObjectSchemaFetcher.DBType.SQLServer
        );

        fetcher.fetch();

        SQLObjectSchema schema = fetcher.getSQLObjectSchema();

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
        SQLObjectSchemaFetcher fetcher = new SQLObjectSchemaFetcher(
            "localhost",
            1433,
            "SA", "PasswordO1.", "test_db",
            List.of("test_schema", "ref_test_schema"),
            SQLObjectSchemaFetcher.DBType.SQLServer
        );

        fetcher.fetch();

        SQLObjectSchema schema = fetcher.getSQLObjectSchema();

        Table testedTable = schema.getTables().stream().filter(s -> s.getTableName().equals("test")).findFirst().orElseThrow();
        assertThat(testedTable.getAttributes()).contains(attribute);
    }

    public static Stream<Arguments> attributesProvider() {
        return Stream.of(
            Arguments.of(named("SQLBit", new Attribute("btAttr", new SQLBit()))),
            Arguments.of(named("SQLChar with default NEWID()", new Attribute("chAttr", new SQLChar(255), false, new SQLFunction("newid")))), // Not null since it is a PK
            Arguments.of(named("SQLDateTime", new Attribute("dtAttr", new SQLDateTime(7)))),
            Arguments.of(named("SQLFloat", new Attribute("dpAttr", new SQLFloat(53)))), // SQLServer converts double precision to float(53)
            Arguments.of(named("SQLReal", new Attribute("flAttr", new SQLReal()))),  // SQLServer converts floats with precision to reals
            Arguments.of(named("SQLInt with default value", new Attribute("itAttr", new SQLInt(), false, new SQLPrimitiveInteger(1)))), // Not null since it is a PK
            Arguments.of(named("SQLReal", new Attribute("rlAttr", new SQLReal(), false))),
            Arguments.of(named("SQLSmallint", new Attribute("siAttr", new SQLSmallint()))),
            Arguments.of(named("SQLVarchar", new Attribute("vcAttr", new SQLVarchar(64)))),
            Arguments.of(named("SQLReal", new Attribute("selfRl", new SQLReal())))
        );
    }
}
