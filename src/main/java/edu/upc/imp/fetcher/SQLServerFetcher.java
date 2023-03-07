package edu.upc.imp.fetcher;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.SchemaReference;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.builders.TableBuilder;
import edu.upc.imp.sqlobjectschema.constraints.PrimaryKey;
import edu.upc.imp.sqlobjectschema.sql_data_types.*;
import org.springframework.jdbc.core.JdbcTemplate;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.sql.JDBCType;
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

        // TODO: Solve parsing expressions and add next two functions following the examples above.
        // Read Defaults
        // Read Checks

        return tables.values().stream().map(TableBuilder::getTable).toList();
    }

    public void readAttributes(SchemaReference schema, Map<String, TableBuilder> tables) {
        // Execute SQL statement to obtain information about attributes
        String statement =
            "select tab.name as table_name, col.name as column_name, ty.name as data_type, col.max_length as [length], col.[precision], col.[scale], col.is_nullable as nullable\n" +
            "from sys.tables tab join sys.columns col on (tab.object_id = col.object_id) join sys.types ty on (col.system_type_id = ty.system_type_id)\n" +
            "where SCHEMA_NAME(tab.schema_id) = '" + schema.getSchemaName() + "'\n" +
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

            SQLDataType dataType = createDataTypeForName(type, length, precision, scale);

            TableBuilder tb = tables.get(tableName);
            if (tb == null) {
                tb = new TableBuilder(tableName, schema);
                tb.addAttribute(attrName, nullable, dataType);
                tables.put(tableName, tb);
            } else {
                tb.addAttribute(attrName, nullable, dataType);
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
            "order by foreign_table, fk_constraint_name;";
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
            Table refTable = schema.getTables().stream().filter(t -> t.getTableName().equalsIgnoreCase(refTableName)).findFirst().orElseThrow();
            Attribute refAttribute = refTable.getAttributes().stream().filter(a -> a.getName().equals(refAttrName)).findFirst().orElseThrow();
            tb.addForeignKeyConstraint(constraintName, attrName, refAttribute);
        }
    }

    /* SQL Queries to obtain all necessary info to build tables, attributes, and constraints

-- Checks
select tab.name as table_name, chck.name as constraint_name, definition as value
from sys.check_constraints chck
	join sys.tables tab on (chck.parent_object_id = tab.object_id)
where SCHEMA_NAME(chck.schema_id) = 'user_schema'
order by table_name, constraint_name;

-- Defaults
select tab.name as table_name, def.name as constraint_name, col.name as column_name, definition as value
from sys.default_constraints def
	join sys.tables tab on (def.parent_object_id = tab.object_id)
	join sys.columns col on (tab.object_id = col.object_id and def.parent_column_id = col.column_id)
where SCHEMA_NAME(def.schema_id) = 'user_schema'
order by table_name, constraint_name;

    */
}
