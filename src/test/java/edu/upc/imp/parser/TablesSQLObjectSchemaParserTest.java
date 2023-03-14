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

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TablesSQLObjectSchemaParserTest {

    /* CREATE TABLE WITHOUT CONSTRAINTS */

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

        assertThrows(SQLObjectAlreadyExistsException.class, () -> parser.parse(basicCreateTable+basicCreateTable));
    }

    @Test
    public void parsingMultipleTablesWithExactNamesRaisesAnExceptionInDifferentCalls() {
        String basicCreateTable = "CREATE TABLE tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable);

        assertThrows(SQLObjectAlreadyExistsException.class, () -> parser.parse(basicCreateTable));
    }

    @Test
    public void parsingMultipleTablesWithExactNamesAndSchemaReferencesRaisesAnException() {
        String basicCreateTable = "CREATE TABLE db.s1.tableName (col1 int, col2 float);";
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(basicCreateTable);

        assertThrows(SQLObjectAlreadyExistsException.class, () -> parser.parse(basicCreateTable));
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

    @Test
    public void parsingTableWithRepeatedAttributesRaisesException() {
        String createTable = """
            CREATE TABLE tableName (
                col int,
                col int
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();

        assertThrows(SQLObjectAlreadyExistsException.class, () -> parser.parse(createTable));
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

        assertThrows(SQLObjectAlreadyExistsException.class, () -> parser.parse(createTable));
    }


    /* COLUMN CONSTRAINTS */

    /** Coupled to parser naming of unnamed constraints! **/
    @Test
    public void parseTableWithDefaultColumnConstraints() {
        String createTable = """
            CREATE TABLE name (
                col1 int DEFAULT 1
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Attribute a1 = new Attribute("col1", new SQLInt(), new SQLPrimitiveInteger(1));

        Table expectedTable = new Table(
            "name",
            null,
            List.of(a1),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );

        assertThat("Parsed table does not equal expected table.",
            schema.getTables().get(0).equals(expectedTable));
    }

    /** Coupled to parser naming of unnamed constraints! **/
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

        Attribute a1 = new Attribute("col1", new SQLInt(), false); // It is a PK.
        Attribute a2 = new Attribute("col2", new SQLInt());

        Table expectedTable = new Table(
            "name",
            null,
            List.of(a1, a2),
            new ArrayList<>(),
            List.of(new Unique("constraint1", List.of(a2))),
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
                col varchar(10) CONSTRAINT c1 CHECK ( col = 'hello' )
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
                    new ColumnReference("col"),
                    new SQLPrimitiveString("hello")))),
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
            List.of(new ForeignKey("fk1", List.of(linkedAttribute), expectedTableA, List.of(referencedAttribute)))
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

        assertThrows(MissingReferencedObjectException.class, () -> parser.parse(createTable));
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

        assertThrows(MissingReferencedObjectException.class, () -> parser.parse(createTables));
    }


    /* TABLE CONSTRAINTS */

    /** Coupled to parser naming of unnamed constraints! **/
//TODO: change how the builder stores things
    @Disabled
    @Test
    public void parseTableWithPrimaryKeyAndUniqueTableConstraints() {
        String createTable = """
            CREATE TABLE name (
                col1 int,
                col2 int,
                CONSTRAINT pk1 PRIMARY KEY (col1, col2),
                UNIQUE (col2)
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
            List.of(new Unique("constraint1", List.of(a2))),
            List.of(new PrimaryKey("pk1", List.of(a1, a2))),
            new ArrayList<>()
        );

        assertThat("Parsed table does not equal expected table.",
            schema.getTables().get(0).equals(expectedTable));
    }

//TODO: make a constraint with a little bit of sense
    @Test
    public void parseTableWithCheckTableConstraints() {
        String createTable = """
            CREATE TABLE name (
                col1 int,
                col2 int,
                CONSTRAINT c1 CHECK ( col1 = col2 )
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTable);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Table expectedTable = new Table(
            "name",
            null,
            List.of(
                new Attribute("col1", new SQLInt()),
                new Attribute("col2", new SQLInt())),
            List.of(new Check("c1",
                new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.EQ,
                    new ColumnReference("col1"),
                    new ColumnReference("col2")))),
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

//TODO: make a constraint with multiple attributes
    @Test
    public void parseTableWithForeignKeyTableConstraint() {
        String createTables = """
            CREATE TABLE tableA (
                colPk int,
                colAttr1 int
            );
            
            CREATE TABLE tableB (
                colPk int,
                colFk int,
                CONSTRAINT fk1 FOREIGN KEY (colFk) REFERENCES tableA (colAttr1)
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
            List.of(new ForeignKey("fk1", List.of(linkedAttribute), expectedTableA, List.of(referencedAttribute)))
        );

        assertThat("Parsed table 'tableA' does not equal expected table.",
            schema.getTables().get(0).equals(expectedTableA));

        assertThat("Parsed table 'tableB' does not equal expected table.",
            schema.getTables().get(1).equals(expectedTableB));
    }

    /** CONSTRAINTS INTEGRATION **/

//TODO: finish test
    @Test
    @Disabled
    public void parseTableWithMultipleConstraints() {
        String createTables = """
            CREATE TABLE A (
              A_a1 int,
              A_a2 int
            );
            
            CREATE TABLE B (
              B_a1 int DEFAULT 0,
              B_a2 int CONSTRAINT unique1 UNIQUE,
              B_a3 int,
              B_a4 int,
              B_a5 int,
              CONSTRAINT fk1 FOREIGN KEY (B_a1) REFERENCES tableA (A_a1)
            );
            """;
        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(createTables);
        SQLObjectSchema schema = parser.getSQLObjectSchema();

        Attribute referencedAttribute = new Attribute("colAttr1", new SQLInt());
        Attribute linkedAttribute = new Attribute("colFk", new SQLInt());

        // Object built directly in java
        Table expectedTableA = new Table(
            "A",
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
            List.of(new ForeignKey("fk1", List.of(linkedAttribute), expectedTableA, List.of(referencedAttribute)))
        );

        assertThat("Parsed table 'tableA' does not equal expected table.",
            schema.getTables().get(0).equals(expectedTableA));

        assertThat("Parsed table 'tableB' does not equal expected table.",
            schema.getTables().get(1).equals(expectedTableB));
    }
}
