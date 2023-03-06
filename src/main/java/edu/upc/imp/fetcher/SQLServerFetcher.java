package edu.upc.imp.fetcher;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.constraints.PrimaryKey;
import org.springframework.jdbc.core.JdbcTemplate;
import com.microsoft.sqlserver.jdbc.SQLServerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

import java.sql.JDBCType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        getTables(schemaName).forEach(schema::addTable);
    }

    private List<Table> getTables(String schemaName) {
        List<Table> result = new ArrayList<>();
        // Read attributes
        // Read PKs and Uniques
        // Read FKs
        // Read Defaults
        // Read Checks
        return result;
    }

    public Table readTable(String schemaName) {
        return null;
    }

    /* SQL Queries to obtain all necessary info to build tables, attributes, and constraints

-- Tables and columns
select tab.name as table_name, col.name as column_name, ty.name as data_type, col.max_length as "length", col.[precision], col.[scale]
from sys.tables tab join sys.columns col on (tab.object_id = col.object_id) join sys.types ty on (col.system_type_id = ty.system_type_id)
order by table_name, col.column_id;

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

-- Primary keys and Uniques
select tab.[name] as table_name,
    idxctnsr.[name] as constraint_name,
    case when idxctnsr.is_primary_key = 1 then 'PK'
         when idxctnsr.is_unique_constraint = 1 then 'U'
    end as [type],
    col.[name] as column_name
from sys.tables tab
    inner join sys.indexes idxctnsr
        on tab.object_id = idxctnsr.object_id
        and (idxctnsr.is_primary_key = 1 or idxctnsr.is_unique_constraint = 1)
    inner join sys.index_columns ic
        on ic.object_id = idxctnsr.object_id
        and ic.index_id = idxctnsr.index_id
    inner join sys.columns col
        on idxctnsr.object_id = col.object_id
        and col.column_id = ic.column_id
where SCHEMA_NAME(tab.schema_id) = 'user_schema'
order by table_name, constraint_name, col.column_id;

-- Foreign keys
select  fk_tab.name as foreign_table,
		fk.name as fk_constraint_name,
        col_name(fk_cols.parent_object_id , fk_cols.parent_column_id) as foreign_column,
        pk_tab.name as referenced_table,
		col_name(fk_cols.referenced_object_id, fk_cols.referenced_column_id) as referenced_column
from sys.foreign_keys fk
    inner join sys.tables fk_tab
        on fk_tab.object_id = fk.parent_object_id
    inner join sys.tables pk_tab
        on pk_tab.object_id = fk.referenced_object_id
    inner join sys.foreign_key_columns fk_cols
        on fk_cols.constraint_object_id = fk.object_id
where SCHEMA_NAME(fk_tab.schema_id) = 'user_schema'
order by foreign_table, fk_constraint_name;

    */
}
