package edu.upc.imp.parser;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.imp.sqlobjectschema.constraints.*;
import edu.upc.imp.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.imp.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLFloat;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLInt;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLVarchar;
import edu.upc.imp.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.imp.sqlobjectschema.value_expressions.SQLPrimitiveString;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TablesSQLObjectSchemaParserTest {

    @Test
    public void parseSimpleCreateTable() {
        String basicCreateTable = "CREATE TABLE name (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table expectedTable = new Table(
            "name",
            List.of(
                new Attribute("col1", new SQLInt()),
                new Attribute("col2", new SQLFloat()))
        );

        assertThat("Parsed assertion does not equal expected assertion",
            schema.getTables().get(0).equals(expectedTable));
    }

    @Test
    public void parsingMultipleTablesWithExactNamesRaisesAnExceptionInOneCall() {
        String basicCreateTable = "CREATE TABLE tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(SQLObjectAlreadyExistsException.class, () -> {
            parser.parse(basicCreateTable+basicCreateTable);
        });
    }

    @Test
    public void parsingMultipleTablesWithExactNamesRaisesAnExceptionInDifferentCalls() {
        String basicCreateTable = "CREATE TABLE tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable);

        assertThrows(SQLObjectAlreadyExistsException.class, () -> {
            parser.parse(basicCreateTable);
        });
    }

    @Test
    public void parsingMultipleTablesWithExactNamesAndSchemaReferencesRaisesAnException() {
        String basicCreateTable = "CREATE TABLE db.s1.tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable);

        assertThrows(SQLObjectAlreadyExistsException.class, () -> {
            parser.parse(basicCreateTable);
        });
    }

    @Test
    public void parsingMultipleTablesWithExactNamesAndDifferentSchemaReferencesRaisesNoException() {
        String basicCreateTable1 = "CREATE TABLE db.s1.tableName (col1 int, col2 float);";
        String basicCreateTable2 = "CREATE TABLE db.s2.tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable1);

        parser.parse(basicCreateTable2);

        assertThat("The 2 tables didn't get parsed correctly.", parser.getSQLObjectSchema().getTables().size() == 2);

    }

    //TODO: modify how nameless constraints are processed
    @Disabled
    @Test
    public void parseTableWithDefaultColumnConstraints() {
        String createTable = """
            CREATE TABLE name (
                col1 int CONSTRAINT default1 DEFAULT 1,
                col2 int DEFAULT 10
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Attribute a1 = new Attribute("col1", new SQLInt());
        Attribute a2 = new Attribute("col2", new SQLInt());

        Table expectedTable = new Table(
            "name",
            null,
            List.of(a1, a2),
            new ArrayList<>(),
            List.of(
                new Default("default1", a1, new SQLPrimitiveInteger(1)),
                new Default("??", a2, new SQLPrimitiveInteger(10))
                ),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );

        assertThat("Parsed table does not equal expected table.",
            schema.getTables().get(0).equals(expectedTable));
    }

    //TODO: modify how nameless constraints are processed
    @Disabled
    @Test
    public void parseTableWithPrimaryKeyAndUniqueColumnConstraints() {
        String createTable = """
            CREATE TABLE name (
                col1 int CONSTRAINT pk1 PRIMARY KEY,
                col2 int UNIQUE
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Attribute a1 = new Attribute("col1", new SQLInt());
        Attribute a2 = new Attribute("col2", new SQLInt());

        Table expectedTable = new Table(
            "name",
            null,
            List.of(a1, a2),
            new ArrayList<>(),
            new ArrayList<>(),
            List.of(new Unique("??", List.of(a2))),
            List.of(new PrimaryKey("pk1", List.of(a1))),
            new ArrayList<>()
        );

        assertThat("Parsed table does not equal expected table.",
            schema.getTables().get(0).equals(expectedTable));
    }

    @Test
    public void parseTableWithCheckColumnConstraints() {
        String createTable = """
            CREATE TABLE name (
                col varchar(10) CONSTRAINT c1 CHECK ( col5 = 'hello' )
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table expectedTable = new Table(
            "name",
            null,
            List.of(new Attribute("col", new SQLVarchar(10))),
            List.of(new Check("c1",
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference("col5"),
                    new SQLPrimitiveString("hello")))),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );

        assertThat("Parsed table does not equal expected table.",
            schema.getTables().get(0).equals(expectedTable));
    }

    @Test
    public void parseTableWithForeignKeyColumnConstraint() {
        String createTables = """
            CREATE TABLE tableA (
                colPk int,
                colAttr1 int
            );
            
            CREATE TABLE tableB (
                colPk int,
                colFk int CONSTRAINT fk1 FOREIGN KEY REFERENCES tableA (colAttr1)
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTables);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Attribute referencedAttribute = new Attribute("colAttr1", new SQLInt());
        Attribute linkedAttribute = new Attribute("colFk", new SQLInt());

        // Object built directly in java
        Table expectedTableA = new Table(
            "tableA",
            List.of(
                new Attribute("colPk", new SQLInt()),
                referencedAttribute
                )
        );

        Table expectedTableB = new Table(
            "tableB",
            null,
            List.of(
                new Attribute("colPk", new SQLInt()),
                linkedAttribute
            ),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            List.of(new ForeignKey("fk1", List.of(linkedAttribute), List.of(referencedAttribute)))
        );

        assertThat("Parsed table 'tableA' does not equal expected table.",
            schema.getTables().get(0).equals(expectedTableA));

        assertThat("Parsed table 'tableB' does not equal expected table.",
            schema.getTables().get(1).equals(expectedTableB));
    }

    @Test
    public void parsingTableWithMissingForeignKeyReferenceRaisesException() {
        String createTable = """            
            CREATE TABLE tableB (
                colPk int,
                colFk int CONSTRAINT fk1 FOREIGN KEY REFERENCES tableA (colAttr1)
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(MissingReferencedObjectException.class, () -> {
            parser.parse(createTable);
        });
    }

    @Test
    public void parsingTableWithMissingForeignKeyReferenceAttributeRaisesException() {
        String createTables = """
            CREATE TABLE tableA (
                colPk int,
                colAttr1 int
            );
            
            CREATE TABLE tableB (
                colPk int,
                colFk int CONSTRAINT fk1 FOREIGN KEY REFERENCES tableA (nonExistingAttr)
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(MissingReferencedObjectException.class, () -> {
            parser.parse(createTables);
        });
    }

    @Test
    public void parsingTableWithRepeatedAttributesRaisesException() {
        String createTable = """
            CREATE TABLE tableName (
                col int,
                col int
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(SQLObjectAlreadyExistsException.class, () -> {
            parser.parse(createTable);
        });
    }

    @Test
    public void parsingTableWithRepeatedAttributesButWithDifferentTypesRaisesException() {
        String createTable = """
            CREATE TABLE tableName (
                col int,
                col varchar(10)
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(SQLObjectAlreadyExistsException.class, () -> {
            parser.parse(createTable);
        });
    }

    //TODO: add table constraint checks

    //TODO: add a test integrating multiple different constraints
}
