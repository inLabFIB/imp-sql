package edu.upc.imp.fetcher.sql_server;

import edu.upc.imp.fetcher.DatabaseFetcher;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.SchemaReference;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.boolean_expressions.BooleanExpression;
import edu.upc.imp.sqlobjectschema.builders.TableSetBuilder;
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
        getTables(schemaReference).forEach(schema::addTable);
    }

    private List<Table> getTables(SchemaReference schemaReference) {
        TableSetBuilder tableSetBuilder = new TableSetBuilder();

        // Read attributes
        readAttributes(schemaReference, tableSetBuilder);
        // Read PKs and Uniques
        readPrimaryKeysAndUniques(schemaReference, tableSetBuilder);
        // Read FKs
        readForeignKeys(schemaReference, tableSetBuilder);
        // Read Checks
        readChecks(schemaReference, tableSetBuilder);

        return tableSetBuilder.build();
    }

    public void readAttributes(SchemaReference schemaReference, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, col.name as column_name, col.column_id as column_position, ty.name as data_type, col.max_length as [length], col.[precision], col.[scale], col.is_nullable as nullable, def.definition as default_value " +
            "from sys.tables tab join sys.columns col on (tab.object_id = col.object_id) " +
            "    join sys.types ty on (col.system_type_id = ty.system_type_id) " +
            "    left join sys.default_constraints def on (def.parent_object_id = tab.object_id and def.parent_column_id = col.column_id) " +
            "where SCHEMA_NAME(tab.schema_id) = '" + schemaReference.getSchemaName() + "' " +
            "order by table_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String attrName = resultSet.getString("column_name");
            String type = resultSet.getString("data_type");
            int length = resultSet.getInt("length");
            int precision = resultSet.getInt("precision");
            int scale = resultSet.getInt("scale");
            boolean nullable = resultSet.getBoolean("nullable");
            String valueExpression = resultSet.getString("default_value");

            SQLDataType dataType = createDataTypeForName(type, length, precision, scale);

            tableSetBuilder.addAttribute(schemaReference, tableName, attrName, dataType, nullable, valueExpression == null ? null : parseValueExpression(valueExpression, tableName));
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

    public void readPrimaryKeysAndUniques(SchemaReference schemaReference, TableSetBuilder tableSetBuilder) {
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
            "where SCHEMA_NAME(tab.schema_id) = '" + schemaReference.getSchemaName() + "' and (idxctnsr.is_primary_key = 1 or idxctnsr.is_unique_constraint = 1)\n" +
            "order by table_name, constraint_name, col.column_id;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String type = resultSet.getString("type");
            String attrName = resultSet.getString("column_name");

            switch (Objects.requireNonNull(type)) {
                case "PK" -> tableSetBuilder.addPrimaryKeyConstraint(schemaReference, tableName, constraintName, attrName);
                case "U" -> tableSetBuilder.addUniqueConstraint(schemaReference, tableName, constraintName, attrName);
            }
        }
    }

    public void readForeignKeys(SchemaReference schemaReference, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select  fk_tab.name as foreign_table,\n" +
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
            "where SCHEMA_NAME(fk_tab.schema_id) = '" + schemaReference.getSchemaName() + "'\n" +
            "order by foreign_table, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("foreign_table");
            String constraintName = resultSet.getString("constraint_name");
            String attrName = resultSet.getString("foreign_column");
            String referenced_schema = resultSet.getString("referenced_schema");
            String refTableName = resultSet.getString("referenced_table");
            String refAttrName = resultSet.getString("referenced_column");
            tableSetBuilder.addForeignKeyConstraint(schemaReference, tableName, constraintName, attrName,
                new SchemaReference(schemaReference.getDatabaseName(), referenced_schema), refTableName, refAttrName);
        }
    }

    private ValueExpression parseValueExpression(String valueExpression, String tableName) {
        TSQLExpressionGrammarVisitorImpl visitor = visitInputExpression(valueExpression, tableName);
        return visitor.getValueExpression();
    }

    public void readChecks(SchemaReference schemaReference, TableSetBuilder tableSetBuilder) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, chck.name as constraint_name, definition as value\n" +
            "from sys.check_constraints chck\n" +
            "    join sys.tables tab on (chck.parent_object_id = tab.object_id)\n" +
            "where SCHEMA_NAME(chck.schema_id) = '" + schemaReference.getSchemaName() + "'\n" +
            "order by table_name, constraint_name;";
        SqlRowSet resultSet = jdbcTemplate.queryForRowSet(statement);
        // Populate table builders
        while (resultSet.next()) {
            String tableName = resultSet.getString("table_name");
            String constraintName = resultSet.getString("constraint_name");
            String booleanExpression = resultSet.getString("value");

            tableSetBuilder.addCheckConstraint(schemaReference, tableName, new Check(constraintName, parseBooleanExpression(booleanExpression, tableName)));
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
