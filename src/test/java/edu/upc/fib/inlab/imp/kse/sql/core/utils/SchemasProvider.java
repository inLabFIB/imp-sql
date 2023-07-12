package edu.upc.fib.inlab.imp.kse.sql.core.utils;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.Table;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLFloat;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLVarchar;

import java.util.List;

public class SchemasProvider {

    public static String getMyTableSchemaStatements() { return MY_TABLE_CREATE_TABLE_STATEMENTS;}

    public static List<Table> getMyTableSchemaTables() {
        return List.of(
            new Table(
                "myTable",
                List.of(
                    new Attribute("a", new SQLInteger()),
                    new Attribute("b", new SQLInteger())
                )
            ),
            new Table(
                "otherTable",
                List.of(
                    new Attribute("c", new SQLInteger()),
                    new Attribute("d", new SQLInteger())
                )
            )
        );
    }

    public static String getABSchemaStatements() { return A_B_CREATE_TABLE_STATEMENTS;}

    public static List<Table> getABSchemaTables() {
        return List.of(
            new Table(
                "A",
                new SchemaReference("sameSchema"),
                List.of(
                    new Attribute("attr1", new SQLVarchar(100)),
                    new Attribute("fk", new SQLInteger())
                )
            ),
            new Table(
                "B",
                new SchemaReference("sameSchema"),
                List.of(
                    new Attribute("pk", new SQLInteger()),
                    new Attribute("attr2", new SQLVarchar(100)),
                    new Attribute("attr3", new SQLFloat())
                )
            )
        );
    }


    public static String getJoinsSchemaStatements() {
        return JOINS_CREATE_TABLE_STATEMENTS;
    }

    public static List<Table> getJoinsSchemaTables() {
        return List.of(
            new Table("A", List.of(
                new Attribute("A_pk", new SQLInteger()),
                new Attribute("A_a1", new SQLInteger()), new Attribute("A_a2", new SQLInteger()))),
            new Table("B", List.of(
                    new Attribute("B_pk", new SQLInteger()),
                    new Attribute("B_a1", new SQLInteger()), new Attribute("B_a2", new SQLInteger()))),
            new Table("C", List.of(
                    new Attribute("C_pk", new SQLInteger()),
                    new Attribute("C_a1", new SQLInteger()), new Attribute("C_a2", new SQLInteger()))),
            new Table("D", List.of(
                    new Attribute("D_pk", new SQLInteger()),
                    new Attribute("D_a1", new SQLInteger()), new Attribute("D_a2", new SQLInteger()))),
            new Table("E", List.of(
                    new Attribute("E_pk", new SQLInteger()),
                    new Attribute("E_a1", new SQLInteger()), new Attribute("E_a2", new SQLInteger())))
        );
    }


    /* --------------------------- Various Create Table Schemas  --------------------------- */

    private static final String MY_TABLE_CREATE_TABLE_STATEMENTS = """
        CREATE TABLE myTable (
            a int,
            b int,
        );
                
        CREATE TABLE otherTable (
            c int,
            d int,
        );
        """;

    private static final String A_B_CREATE_TABLE_STATEMENTS = """
        CREATE TABLE sameSchema.A (
            attr1 varchar(100),
            fk int,
        );

        CREATE TABLE sameSchema.B (
            pk int,
            attr2 varchar(100),
            attr3 float
        );
        """;

    private static final String JOINS_CREATE_TABLE_STATEMENTS = """
        CREATE TABLE A (A_pk int, A_a1 int, A_a2 inT);
        CREATE TABLE B (B_pk int, B_a1 int, B_a2 inT);
        CREATE TABLE C (C_pk int, C_a1 int, C_a2 inT);
        CREATE TABLE D (D_pk int, D_a1 int, D_a2 inT);
        CREATE TABLE E (E_pk int, E_a1 int, E_a2 inT);
        """;
}
