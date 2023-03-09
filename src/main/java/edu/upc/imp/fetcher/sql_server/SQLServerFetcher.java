package edu.upc.imp.fetcher.sql_server;

import edu.upc.imp.fetcher.DatabaseFetcher;
import edu.upc.imp.parser.SQLObjectSchemaGrammarVisitorImpl;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.SchemaReference;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.imp.sqlobjectschema.builders.TableBuilder;
import edu.upc.imp.sqlobjectschema.constraints.Check;
import edu.upc.imp.sqlobjectschema.sql_data_types.*;
import edu.upc.imp.sqlobjectschema.value_expressions.ValueExpression;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.jdbc.core.JdbcTemplate;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import edu.upc.imp.parser.sql_server.TSqlLexer;
import edu.upc.imp.parser.sql_server.TSqlExpressionParser;

import java.util.*;

public class SQLServerFetcher implements DatabaseFetcher {

    private final JdbcTemplate jdbcTemplate;

    public SQLServerFetcher (String serverName, int port, String dbName, String user, String pwd) {
        SQLServerDataSource dataSource = new SQLServerDataSource();
        dataSource.setServerName(serverName);
        dataSource.setPortNumber(port);
        dataSource.setDatabaseName(dbName);
        dataSource.setUser(user);
        dataSource.setPassword(pwd);
        dataSource.setEncrypt(false);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void fetch(String dbName, String schemaName, SQLObjectSchema schema) {
        SchemaReference schemaReference = new SchemaReference(dbName, schemaName);
        getTables(schemaReference, schema).forEach(schema::addTable);
    }

    private List<Table> getTables(SchemaReference schemaReference, SQLObjectSchema schema) {
        Map<String, TableBuilder> tables = new HashMap<>();

        // Read attributes
        readAttributes(schemaReference, tables);
        // Read PKs and Uniques
        readPrimaryKeysAndUniques(schemaReference.getSchemaName(), tables);
        // Read FKs
        readForeignKeys(schemaReference.getSchemaName(), schema, tables);
        // TODO: Test parsing of two functions bellow.
        // Read Defaults
        readDefaults(schemaReference.getSchemaName(), tables);
        // Read Checks
        readChecks(schemaReference.getSchemaName(), tables);

        // Order tables by FK dependencies:
        List<Table> processedTables = new ArrayList<>();
        while(!tables.isEmpty()) {
            // TODO: Test FK sorting, may not be working.
            TableBuilder nextTableToProcess = tables.values().iterator().next();
            buildTable(nextTableToProcess, tables, new HashSet<>(), processedTables);
        }

        return processedTables;
        //return tables.values().stream().map(t->t.build(new ArrayList<>())).toList();
    }

    private void buildTable(TableBuilder table, Map<String, TableBuilder> tablesToProcess, Set<String> beingProcessed, List<Table> processedTables) {
        if (beingProcessed.contains(table.getTableName())) throw new RuntimeException("Foreign Key dependency cycle");
        beingProcessed.add(table.getTableName());

        List<String> namesToProcess = tablesToProcess.values().stream().map(TableBuilder::getTableName).toList();
        List<String> shouldProcessFirst = table.getTableReferences().stream().filter(namesToProcess::contains).toList();
        for (String s : shouldProcessFirst) {
            TableBuilder nextTableToProcess = tablesToProcess.get(s);
            buildTable(nextTableToProcess, tablesToProcess, beingProcessed, processedTables);
        }

        processedTables.add(table.build(processedTables));
        tablesToProcess.remove(table.getTableName());
    }

    public void readAttributes(SchemaReference schema, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, col.name as column_name, col.column_id as column_position, ty.name as data_type, col.max_length as [length], col.[precision], col.[scale], col.is_nullable as nullable\n" +
            "from sys.tables tab join sys.columns col on (tab.object_id = col.object_id) join sys.types ty on (col.system_type_id = ty.system_type_id)\n" +
            "where SCHEMA_NAME(tab.schema_id) = '" + schema.getSchemaName() + "'\n" +
            "order by table_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String attrName = resultSet.getString("column_name");
            int attrPos = resultSet.getInt("column_position");
            String type = resultSet.getString("data_type");
            int length = resultSet.getInt("length");
            int precision = resultSet.getInt("precision");
            int scale = resultSet.getInt("scale");
            boolean nullable = resultSet.getBoolean("nullable");

            SQLDataType dataType = createDataTypeForName(type, length, precision, scale);

            TableBuilder tb = tables.get(tableName);
            if (tb == null) {
                tb = new TableBuilder(tableName, schema);
                tb.addAttribute(attrName, attrPos, nullable, dataType);
                tables.put(tableName, tb);
            } else {
                tb.addAttribute(attrName, attrPos, nullable, dataType);
            }
        }
    }

    private SQLDataType createDataTypeForName(String type, int length, int precision, int scale) {
        return switch (type) {
            case "bit" -> new SQLBit();
            case "char" -> new SQLChar(length);
            case "datetime2" -> new SQLDate(scale);
            case "float" -> new SQLFloat(precision); // FIXME: Precision does not match SQLServer query...
            case "int" -> new SQLInt();
            case "real" -> new SQLReal();
            case "smallint" -> new SQLSmallint();
            case "varchar" -> new SQLVarchar(length);
            default -> throw new RuntimeException("Table contains an attribute of unsupported data-type.");
        };
    }

    public void readPrimaryKeysAndUniques(String schemaName, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.[name] as table_name,\n" +
            "    idxctnsr.[name] as constraint_name,\n" +
            "    case when idxctnsr.is_primary_key = 1 then 'PK'\n" +
            "         when idxctnsr.is_unique_constraint = 1 then 'U'\n" +
            "    end as [type],\n" +
            "    col.[name] as column_name\n" +
            "from sys.tables tab\n" +
            "    inner join sys.indexes idxctnsr\n" +
            "        on tab.object_id = idxctnsr.object_id\n" +
            "        and (idxctnsr.is_primary_key = 1 or idxctnsr.is_unique_constraint = 1)\n" +
            "    inner join sys.index_columns ic\n" +
            "        on ic.object_id = idxctnsr.object_id\n" +
            "        and ic.index_id = idxctnsr.index_id\n" +
            "    inner join sys.columns col\n" +
            "        on idxctnsr.object_id = col.object_id\n" +
            "        and col.column_id = ic.column_id\n" +
            "where SCHEMA_NAME(tab.schema_id) = '" + schemaName + "' and (idxctnsr.is_primary_key = 1 or idxctnsr.is_unique_constraint = 1)\n" +
            "order by table_name, constraint_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String type = resultSet.getString("type");
            String attrName = resultSet.getString("column_name");

            TableBuilder tb = tables.get(tableName);
            if (tb == null) continue; // Somehow detected a constraint on a table not yet found (with no attributes). Maybe should create an empty table builder?
            switch (Objects.requireNonNull(type)) {
                case "PK" -> tb.addPrimaryKeyConstraint(constraintName, attrName);
                case "U" -> tb.addUniqueConstraint(constraintName, attrName);
            }
        }
    }

    public void readForeignKeys(String schemaName, SQLObjectSchema schema, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select  fk_tab.name as foreign_table,\n" +
            "        fk.name as constraint_name,\n" +
            "        col_name(fk_cols.parent_object_id , fk_cols.parent_column_id) as foreign_column,\n" +
            "        pk_tab.name as referenced_table,\n" +
            "        col_name(fk_cols.referenced_object_id, fk_cols.referenced_column_id) as referenced_column\n" +
            "from sys.foreign_keys fk\n" +
            "    inner join sys.tables fk_tab\n" +
            "        on fk_tab.object_id = fk.parent_object_id\n" +
            "    inner join sys.tables pk_tab\n" +
            "        on pk_tab.object_id = fk.referenced_object_id\n" +
            "    inner join sys.foreign_key_columns fk_cols\n" +
            "        on fk_cols.constraint_object_id = fk.object_id\n" +
            "where SCHEMA_NAME(fk_tab.schema_id) = '" + schemaName + "'\n" +
            "order by foreign_table, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("foreign_table");
            String constraintName = resultSet.getString("constraint_name");
            String attrName = resultSet.getString("foreign_column");
            String refTableName = resultSet.getString("referenced_table");
            String refAttrName = resultSet.getString("referenced_column");

            TableBuilder tb = tables.get(tableName);
            if (tb == null) continue; // Somehow detected a constraint on a table not yet found (with no attributes). Maybe should create an empty table builder?
            tb.addForeignKeyConstraint(constraintName, attrName, refTableName, refAttrName);
        }
    }

    public void readDefaults(String schemaName, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, def.name as constraint_name, col.name as column_name, definition as value\n" +
            "from sys.default_constraints def\n" +
            "    join sys.tables tab on (def.parent_object_id = tab.object_id)\n" +
            "    join sys.columns col on (tab.object_id = col.object_id and def.parent_column_id = col.column_id)\n" +
            "where SCHEMA_NAME(def.schema_id) = '" + schemaName + "'\n" +
            "order by table_name, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String attrName = resultSet.getString("column_name");
            String valueExpression = resultSet.getString("value");

            TableBuilder tb = tables.get(tableName);
            if (tb == null) continue; // Somehow detected a constraint on a table not yet found (with no attributes). Maybe should create an empty table builder?
            tb.addDefaultConstraint(constraintName, attrName, parseValueExpression(valueExpression, tableName));
        }
    }

    private ValueExpression parseValueExpression(String valueExpression, String tableName) {
        CodePointCharStream input = CharStreams.fromString(valueExpression);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlExpressionParser parser = new TSqlExpressionParser(tokens);

        TSqlExpressionParser.ExpressionContext tree = parser.expression();
        TSQLExpressionGrammarVisitorImpl visitor = new TSQLExpressionGrammarVisitorImpl(tableName);
        visitor.visit(tree);
        return visitor.getValueExpression();
    }

    public void readChecks(String schemaName, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, chck.name as constraint_name, definition as value\n" +
            "from sys.check_constraints chck\n" +
            "    join sys.tables tab on (chck.parent_object_id = tab.object_id)\n" +
            "where SCHEMA_NAME(chck.schema_id) = '" + schemaName + "'\n" +
            "order by table_name, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String booleanExpression = resultSet.getString("value");

            TableBuilder tb = tables.get(tableName);
            if (tb == null) continue; // Somehow detected a constraint on a table not yet found (with no attributes). Maybe should create an empty table builder?
            tb.addCheckConstraint(new Check(constraintName, parseBooleanExpression(booleanExpression, tableName)));
        }
    }

    private BooleanExpression parseBooleanExpression(String booleanExpression, String tableName) {
        CodePointCharStream input = CharStreams.fromString(booleanExpression);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlExpressionParser parser = new TSqlExpressionParser(tokens);

        TSqlExpressionParser.ExpressionContext tree = parser.expression();
        TSQLExpressionGrammarVisitorImpl visitor = new TSQLExpressionGrammarVisitorImpl(tableName);
        visitor.visit(tree);
        return visitor.getBooleanExpression();
    }
}
