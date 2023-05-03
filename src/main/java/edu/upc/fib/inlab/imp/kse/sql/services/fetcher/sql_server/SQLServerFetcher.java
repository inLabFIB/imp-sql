package edu.upc.fib.inlab.imp.kse.sql.services.fetcher.sql_server;

import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import edu.upc.fib.inlab.imp.kse.sql.parser.sql_server.TSqlExpressionParser;
import edu.upc.fib.inlab.imp.kse.sql.parser.sql_server.TSqlLexer;
import edu.upc.fib.inlab.imp.kse.sql.services.builders.TableSetBuilder;
import edu.upc.fib.inlab.imp.kse.sql.services.fetcher.DatabaseFetcher;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    public SQLServerFetcher (DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public void fetch(String dbName, List<String> schemaNames, SQLObjectSchema schema) {
        List<SchemaReference> schemaReferences = schemaNames.stream().map(s -> new SchemaReference(dbName, s)).toList();
        getTables(schemaReferences).forEach(schema::addTable);
    }

    private List<Table> getTables(List<SchemaReference> schemaReferences) {
        Map<String, SchemaReference> schemasMap = new HashMap<>();
        schemaReferences.forEach(s -> schemasMap.put(s.getSchemaName(), s));

        TableSetBuilder tableSetBuilder = new TableSetBuilder();
        // Read attributes
        readAttributes(schemasMap, tableSetBuilder);
        // Read PKs and Uniques
        readPrimaryKeysAndUniques(schemasMap, tableSetBuilder);
        // Read FKs
        readForeignKeys(schemasMap, tableSetBuilder);
        // Read Checks
        readChecks(schemasMap, tableSetBuilder);

        return tableSetBuilder.build();
    }

    public void readAttributes(Map<String, SchemaReference> schemasMap, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String inConditionString = "('" + String.join("','", schemasMap.keySet()) + "')";
        String statement =
            "select SCHEMA_NAME(tab.schema_id) as schema_name, tab.name as table_name, col.name as column_name, col.column_id as column_position, ty.name as data_type, col.max_length as [length], col.[precision], col.[scale], col.is_nullable as nullable, def.definition as default_value " +
            "from sys.tables tab join sys.columns col on (tab.object_id = col.object_id) " +
            "    join sys.types ty on (col.system_type_id = ty.system_type_id) " +
            "    left join sys.default_constraints def on (def.parent_object_id = tab.object_id and def.parent_column_id = col.column_id) " +
            "where SCHEMA_NAME(tab.schema_id) in " + inConditionString + " " +
            "order by table_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String schemaName = resultSet.getString("schema_name");
            String tableName = resultSet.getString("table_name");
            String attrName = resultSet.getString("column_name");
            String type = resultSet.getString("data_type");
            int length = resultSet.getInt("length");
            int precision = resultSet.getInt("precision");
            int scale = resultSet.getInt("scale");
            boolean nullable = resultSet.getBoolean("nullable");
            String valueExpression = resultSet.getString("default_value");

            assert type != null;
            SQLDataType dataType = createDataTypeForName(type, length, precision, scale);

            tableSetBuilder.addAttribute(schemasMap.get(schemaName), tableName, attrName, dataType, nullable, valueExpression == null ? null : parseValueExpression(valueExpression, tableName));
        }
    }

    private SQLDataType createDataTypeForName(String type, int length, int precision, int scale) {
        return switch (type) {
            case "bit" -> new SQLBit();
            case "char" -> new SQLChar(length);

            case "date" -> new SQLDate();
            case "datetime" -> new SQLDateTime(scale);
            case "datetime2" -> new SQLDateTime(scale); //FIXME: possible errors in precision or date ranges

            case "float" -> new SQLFloat(precision); // FIXME: Precision does not match SQLServer query...

            case "decimal", "numeric" -> new SQLNumeric(precision, scale);
            case "bigint" -> new SQLNumeric(precision, scale); // length should be 8 Bytes
            case "int" -> new SQLInt();
            case "smallint" -> new SQLSmallint();

            case "real" -> new SQLReal();

            case "varchar" -> new SQLVarchar(length);

            default -> throw new RuntimeException("Table contains an attribute of unsupported data-type (" + type + ").");
        };
    }

    public void readPrimaryKeysAndUniques(Map<String, SchemaReference> schemasMap, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String inConditionString = "('" + String.join("','", schemasMap.keySet()) + "')";
        String statement =
            "select SCHEMA_NAME(tab.schema_id) as schema_name,\n" +
            "    tab.[name] as table_name,\n" +
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
            "where SCHEMA_NAME(tab.schema_id) in " + inConditionString + " and (idxctnsr.is_primary_key = 1 or idxctnsr.is_unique_constraint = 1)\n" +
            "order by table_name, constraint_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String schemaName = resultSet.getString("schema_name");
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String type = resultSet.getString("type");
            String attrName = resultSet.getString("column_name");

            switch (Objects.requireNonNull(type)) {
                case "PK" -> tableSetBuilder.addPrimaryKeyConstraint(schemasMap.get(schemaName), tableName, constraintName, attrName);
                case "U" -> tableSetBuilder.addUniqueConstraint(schemasMap.get(schemaName), tableName, constraintName, attrName);
            }
        }
    }

    public void readForeignKeys(Map<String, SchemaReference> schemasMap, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String inConditionString = "('" + String.join("','", schemasMap.keySet()) + "')";
        String statement =
            "select  SCHEMA_NAME(fk_tab.schema_id) as foreign_schema,\n" +
            "        fk_tab.name as foreign_table,\n" +
            "        fk.name as constraint_name,\n" +
            "        col_name(fk_cols.parent_object_id , fk_cols.parent_column_id) as foreign_column,\n" +
            "        SCHEMA_NAME(pk_tab.schema_id) as referenced_schema,\n" +
            "        pk_tab.name as referenced_table,\n" +
            "        col_name(fk_cols.referenced_object_id, fk_cols.referenced_column_id) as referenced_column\n" +
            "from sys.foreign_keys fk\n" +
            "    inner join sys.tables fk_tab\n" +
            "        on fk_tab.object_id = fk.parent_object_id\n" +
            "    inner join sys.tables pk_tab\n" +
            "        on pk_tab.object_id = fk.referenced_object_id\n" +
            "    inner join sys.foreign_key_columns fk_cols\n" +
            "        on fk_cols.constraint_object_id = fk.object_id\n" +
            "where SCHEMA_NAME(fk_tab.schema_id) in " + inConditionString + "\n" +
            "order by foreign_table, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String schemaName = resultSet.getString("foreign_schema");
            String tableName = resultSet.getString("foreign_table");
            String constraintName = resultSet.getString("constraint_name");
            String attrName = resultSet.getString("foreign_column");
            String referenced_schema = resultSet.getString("referenced_schema");
            String refTableName = resultSet.getString("referenced_table");
            String refAttrName = resultSet.getString("referenced_column");
            tableSetBuilder.addForeignKeyConstraint(schemasMap.get(schemaName), tableName, constraintName, attrName,
                new SchemaReference(schemasMap.get(schemaName).getDatabaseName(), referenced_schema), refTableName, refAttrName);
        }
    }

    private ValueExpression parseValueExpression(String valueExpression, String tableName) {
        TSQLExpressionGrammarVisitorImpl visitor = visitInputExpression(valueExpression, tableName);
        return visitor.getValueExpression();
    }

    public void readChecks(Map<String, SchemaReference> schemasMap, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String inConditionString = "('" + String.join("','", schemasMap.keySet()) + "')";
        String statement =
            "select SCHEMA_NAME(chck.schema_id) as schema_name, tab.name as table_name, chck.name as constraint_name, definition as value\n" +
            "from sys.check_constraints chck\n" +
            "    join sys.tables tab on (chck.parent_object_id = tab.object_id)\n" +
            "where SCHEMA_NAME(chck.schema_id) in " + inConditionString + "\n" +
            "order by table_name, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String schemaName = resultSet.getString("schema_name");
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String booleanExpression = resultSet.getString("value");

            tableSetBuilder.addCheckConstraint(schemasMap.get(schemaName), tableName, new Check(constraintName, parseBooleanExpression(booleanExpression, tableName)));
        }
    }

    private BooleanExpression parseBooleanExpression(String booleanExpression, String tableName) {
        TSQLExpressionGrammarVisitorImpl visitor = visitInputExpression(booleanExpression, tableName);
        return visitor.getBooleanExpression();
    }

    private TSQLExpressionGrammarVisitorImpl visitInputExpression(String booleanExpression, String tableName) {
        CodePointCharStream input = CharStreams.fromString(booleanExpression);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlExpressionParser parser = new TSqlExpressionParser(tokens);

        TSqlExpressionParser.ExpressionContext tree = parser.expression();
        TSQLExpressionGrammarVisitorImpl visitor = new TSQLExpressionGrammarVisitorImpl(tableName);
        visitor.visit(tree);
        return visitor;
    }
}
