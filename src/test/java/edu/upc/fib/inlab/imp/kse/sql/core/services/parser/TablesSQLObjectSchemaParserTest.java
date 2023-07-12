package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.Table;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.PredicateOperation;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.exceptions.MissingReferencedObjectException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLBit;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLFloat;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.sql_data_types.SQLVarchar;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLFunction;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.SQLPrimitiveString;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TablesSQLObjectSchemaParserTest {

    @Nested
    class TableParsingThrowsException {

        @Nested
        class RepeatedTableOrAttributeNamesTests {
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
        }
    }

    @Nested
    class TableWithoutConstraintsTest {
        @Test
        public void parseSimpleCreateTable() {
            String basicCreateTable = "CREATE TABLE name (col1 int, col2 float);";
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(basicCreateTable);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Table expectedTable = new Table(
                "name",
                List.of(
                    new Attribute("col1", new SQLInteger()),
                    new Attribute("col2", new SQLFloat()))
            );

            assertThat("Parsed assertion does not equal expected assertion",
                schema.getTables().get(0).equals(expectedTable));
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
    }

    @Nested
    class TableWithColumConstraintsTest {
        /** Coupled to parser naming of unnamed constraints! **/
        @Test
        public void parseTableWithDefaultColumnConstraints() {
            String createTable = """
            CREATE TABLE name (
                col1 int DEFAULT 1,
                col2 int DEFAULT MYFUNCTION()
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTable);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Attribute a1 = new Attribute("col1", new SQLInteger(), new SQLPrimitiveInteger(1));
            Attribute a2 = new Attribute("col2", new SQLInteger(), new SQLFunction("MYFUNCTION"));

            Table expectedTable = new Table(
                "name",
                null,
                List.of(a1, a2),
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

            Attribute a1 = new Attribute("col1", new SQLInteger(), false); // It is a PK.
            Attribute a2 = new Attribute("col2", new SQLInteger());

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

            Attribute referencedAttribute = new Attribute("colAttr1", new SQLInteger());
            Attribute linkedAttribute = new Attribute("colFk", new SQLInteger());

            // Object built directly in java
            Table expectedTableA = new Table(
                "tableA",
                List.of(
                    new Attribute("colPk", new SQLInteger()),
                    referencedAttribute
                )
            );

            Table expectedTableB = new Table(
                "tableB",
                null,
                List.of(
                    new Attribute("colPk", new SQLInteger()),
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
    }

    @Nested
    class TablesWithTableConstraintsTests {
        /** Coupled to parser naming of unnamed constraints! **/
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

            Attribute a1 = new Attribute("col1", new SQLInteger(), false);
            Attribute a2 = new Attribute("col2", new SQLInteger(), false);

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

        //TODO: when adding OR, and TRUE, FALSE change the constraint to {col1 > 18 OR col2 = FALSE}
        @Test
        public void parseTableWithCheckTableConstraints() {
            String createTable = """
            CREATE TABLE name (
                col1 int,
                col2 bit,
                CONSTRAINT c1 CHECK ( col1 > 18 AND col2 = 1 )
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTable);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Table expectedTable = new Table(
                "name",
                null,
                List.of(
                    new Attribute("col1", new SQLInteger()),
                    new Attribute("col2", new SQLBit())),
                List.of(new Check("c1",
                    new PredicateOperation(
                        PredicateOperation.PredicateOperator.AND,
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.GT,
                            new ColumnReference("col1"),
                            new SQLPrimitiveInteger(18)),
                        new ComparisonPredicate(
                            ComparisonPredicate.ComparisonOperator.EQ,
                            new ColumnReference("col2"),
                            new SQLPrimitiveInteger(1))
                    )
                )),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
            );

            assertThat("Parsed table does not equal expected table.",
                schema.getTables().get(0).equals(expectedTable));
        }

        @Test
        public void parseTableWithForeignKeyTableConstraint() {
            String createTables = """
            CREATE TABLE tableA (
                colPk int,
                colAttr1 int,
                colAttr2 int
            );
            
            CREATE TABLE tableB (
                colPk int,
                colFk1 int,
                colFk2 int,
                CONSTRAINT fk1 FOREIGN KEY (colFk1, colFk2) REFERENCES tableA (colAttr1, colAttr2)
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTables);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Attribute referencedAttribute1 = new Attribute("colAttr1", new SQLInteger());
            Attribute referencedAttribute2 = new Attribute("colAttr2", new SQLInteger());
            Attribute linkedAttribute1 = new Attribute("colFk1", new SQLInteger());
            Attribute linkedAttribute2 = new Attribute("colFk2", new SQLInteger());

            // Object built directly in java
            Table expectedTableA = new Table(
                "tableA",
                List.of(
                    new Attribute("colPk", new SQLInteger()),
                    referencedAttribute1,
                    referencedAttribute2
                )
            );

            Table expectedTableB = new Table(
                "tableB",
                null,
                List.of(
                    new Attribute("colPk", new SQLInteger()),
                    linkedAttribute1,
                    linkedAttribute2
                ),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                List.of(new ForeignKey("fk1",
                    List.of(linkedAttribute1, linkedAttribute2),
                    expectedTableA, List.of(referencedAttribute1, referencedAttribute2)))
            );

            assertThat("Parsed table 'tableA' does not equal expected table.",
                schema.getTables().get(0).equals(expectedTableA));

            assertThat("Parsed table 'tableB' does not equal expected table.",
                schema.getTables().get(1).equals(expectedTableB));
        }

        @Test
        public void parseAutoReferencingForeignKeyConstraint() {
            String createTables = """
            CREATE TABLE A (
                colPk int,
                colFk int,
                CONSTRAINT fk1 FOREIGN KEY (colFk) REFERENCES A (colPk)
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTables);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            List<ForeignKey> foreignKeys = schema.getTables().get(0).getForeignKeyConstraints();
            assertThat("Foreign Key not parsed.", foreignKeys.size() == 1);
            assertThat("Table loop not found.", schema.getTables().get(0).equals(foreignKeys.get(0).getPkReferenceTable()));
        }

        @Test
        public void parseLoopingForeignKeyConstraints() {
            String createTables = """
            CREATE TABLE [schema1].A (
                colPk int,
                colFk int,
                CONSTRAINT fk1 FOREIGN KEY (colFk) REFERENCES [schema2].B (colPk)
            );
            
            CREATE TABLE [schema2].B (
                colPk int,
                colFk int,
                CONSTRAINT fk1 FOREIGN KEY (colFk) REFERENCES [schema1].A (colPk)
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTables);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Table tableA = schema.getTables().get(0);
            Table tableB = schema.getTables().get(1);

            assertThat("Foreign Key of table A not parsed.", tableA.getForeignKeyConstraints().size() == 1);
            assertThat("Foreign Key of table B not parsed.", tableB.getForeignKeyConstraints().size() == 1);

            assertThat("Foreign Key loop not found.", tableA.equals(tableB.getForeignKeyConstraints().get(0).getPkReferenceTable()));
            assertThat("Foreign Key loop not found.", tableB.equals(tableA.getForeignKeyConstraints().get(0).getPkReferenceTable()));
        }

        /** CONSTRAINTS INTEGRATION **/

        //TODO: when adding more expressions add them to the test (OR, boolean types,...)
        @Test
        public void parseTableWithMultipleConstraints() {
            String createTables = """
            CREATE TABLE A (
              A_a1 int,
              A_a2 int
            );
            
            CREATE TABLE B (
              B_a1 int,
              B_a2 int,
              B_a3 int DEFAULT 0,
              B_a4 int CONSTRAINT unique1 UNIQUE,
              B_a5 int,
              CONSTRAINT pk1 PRIMARY KEY (B_a1),
              CONSTRAINT fk1 FOREIGN KEY (B_a2) REFERENCES A (A_a1),
              CONSTRAINT C1 CHECK (B_a5 < > B_a4)
            );
            """;
            SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
            parser.parse(createTables);
            SQLObjectSchema schema = parser.getSQLObjectSchema();

            Attribute pkAttribute1 = new Attribute("B_a1", new SQLInteger(), false);
            Attribute uniqueAttribute1 = new Attribute("B_a4", new SQLInteger());
            Attribute referencedAttribute1 = new Attribute("A_a1", new SQLInteger());
            Attribute linkedAttribute1 = new Attribute("B_a2", new SQLInteger());

            // Object built directly in java
            Table expectedTableA = new Table(
                "A",
                List.of(
                    referencedAttribute1,
                    new Attribute("A_a2", new SQLInteger())
                )
            );

            Table expectedTableB = new Table(
                "B",
                null,
                List.of(
                    pkAttribute1,
                    linkedAttribute1,
                    new Attribute("B_a3", new SQLInteger(), new SQLPrimitiveInteger(0)),
                    uniqueAttribute1,
                    new Attribute("B_a5", new SQLInteger())
                ),
                List.of(new Check("C1", new ComparisonPredicate(
                    ComparisonPredicate.ComparisonOperator.NEQ,
                    new ColumnReference("B_a5"),
                    new ColumnReference("B_a4")))),
                List.of(new Unique("unique1", List.of(uniqueAttribute1))),
                List.of(new PrimaryKey("pk1", List.of(pkAttribute1))),
                List.of(new ForeignKey("fk1", List.of(linkedAttribute1), expectedTableA, List.of(referencedAttribute1)))
            );

            assertThat("Parsed table 'tableA' does not equal expected table.",
                schema.getTables().get(0).equals(expectedTableA));

            assertThat("Parsed table 'tableB' does not equal expected table.",
                schema.getTables().get(1).equals(expectedTableB));
        }
    }

}
