/*
T-SQL (Transact-SQL, MSSQL) grammar.
The MIT License (MIT).
Copyright (c) 2017, Mark Adams (madams51703@gmail.com)
Copyright (c) 2015-2017, Ivan Kochurkin (kvanttt@gmail.com), Positive Technologies.
Copyright (c) 2016, Scott Ure (scott@redstormsoftware.com).
Copyright (c) 2016, Rui Zhang (ruizhang.ccs@gmail.com).
Copyright (c) 2016, Marcus Henriksson (kuseman80@gmail.com).
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

parser grammar TSqlParser;

options { tokenVocab=TSqlLexer; }

tsql_file
    : batch* EOF
    | execute_body_batch go_statement* EOF
    ;

batch
    : go_statement
    | execute_body_batch? (go_statement | sql_clauses+) go_statement*
    | batch_level_statement go_statement*
    ;

batch_level_statement
    : create_view
    ;

sql_clauses
    : dml_clause SEMI?
    | ddl_clause SEMI?
    | SEMI
    ;

// Data Manipulation Language: https://msdn.microsoft.com/en-us/library/ff848766(v=sql.120).aspx
dml_clause
    : select_statement_standalone
    ;

// Data Definition Language: https://msdn.microsoft.com/en-us/library/ff848799.aspx)
ddl_clause
    : create_assertion
    ;

file_path
    : file_directory_path_separator file_path
    | id_
    ;

file_directory_path_separator
    : '\\'
    ;

event_session_predicate_expression
    : ( COMMA? (AND|OR)? NOT? ( event_session_predicate_factor | LR_BRACKET event_session_predicate_expression RR_BRACKET) )+
    ;

event_session_predicate_factor
    : event_session_predicate_leaf
    | LR_BRACKET event_session_predicate_expression RR_BRACKET
    ;

event_session_predicate_leaf
    : (event_field_name=id_ | (event_field_name=id_ |( (event_module_guid=id_ DOT)?  event_package_name=id_ DOT predicate_source_name=id_ ) ) (EQUAL |(LESS GREATER) | (EXCLAMATION EQUAL) | GREATER  | (GREATER EQUAL)| LESS | LESS EQUAL) (DECIMAL | STRING) )
    | (event_module_guid=id_ DOT)?  event_package_name=id_ DOT predicate_compare_name=id_ LR_BRACKET (event_field_name=id_ |( (event_module_guid=id_ DOT)?  event_package_name=id_ DOT predicate_source_name=id_ ) COMMA  (DECIMAL | STRING) ) RR_BRACKET
    ;

// https://msdn.microsoft.com/en-us/library/ms189499.aspx
select_statement_standalone
    : with_expression? select_statement
    ;

select_statement
    : query_expression select_order_by_clause? for_clause? option_clause? ';'?
    ;

// DDL

//TODO: add [constraint_attributes]
create_assertion
    : CREATE ASSERTION simple_name assertion_check ';'?
    ;

assertion_check
    : CHECK LR_BRACKET search_condition RR_BRACKET
    ;


// https://msdn.microsoft.com/en-us/library/ms187956.aspx
create_view
    : CREATE VIEW simple_name ('(' column_name_list ')')?
      (WITH view_attribute (',' view_attribute)*)?
      AS select_statement_standalone (WITH CHECK OPTION)? ';'?
    ;

view_attribute
    : ENCRYPTION | SCHEMABINDING | VIEW_METADATA
    ;

host
    : id_ DOT host
    | (id_ DOT |id_)
    ;

execute_body_batch
    : func_proc_name_server_database_schema (execute_statement_arg (',' execute_statement_arg)*)? ';'?
    ;

execute_statement_arg
    :
    execute_statement_arg_unnamed (',' execute_statement_arg) *    //Unnamed params can continue unnamed
    |
    execute_statement_arg_named (',' execute_statement_arg_named)* //Named can only be continued by unnamed
    ;

execute_statement_arg_named
    : name=LOCAL_ID '=' value=execute_parameter
    ;

execute_statement_arg_unnamed
    : value=execute_parameter
    ;

execute_parameter
    : (constant | LOCAL_ID (OUTPUT | OUT)? | id_ | DEFAULT | NULL_)
    ;

execute_var_string
    : LOCAL_ID (OUTPUT | OUT)? ('+' LOCAL_ID ('+' execute_var_string)?)?
    | STRING ('+' LOCAL_ID ('+' execute_var_string)?)?
    ;

// https://msdn.microsoft.com/en-us/library/ms188037.aspx
go_statement
    : GO (count=DECIMAL)?
    ;

// Expression.

// https://docs.microsoft.com/en-us/sql/t-sql/language-elements/expressions-transact-sql
// Operator precendence: https://docs.microsoft.com/en-us/sql/t-sql/language-elements/operator-precedence-transact-sql
expression
    : primitive_expression
    | function_call
    | expression '.' (value_call | query_call | exist_call | modify_call)
    | expression '.' hierarchyid_call
    | expression COLLATE id_
    | case_expression
    | full_column_name
    | bracket_expression
    | unary_operator_expression
    | expression op=('*' | '/' | '%') expression
    | expression op=('+' | '-' | '&' | '^' | '|' | '||') expression
    | expression time_zone
    | over_clause
    | DOLLAR_ACTION
    ;

parameter
    : PLACEHOLDER;

time_zone
    : AT_KEYWORD TIME ZONE expression
    ;

primitive_expression
    : DEFAULT | NULL_ | LOCAL_ID | primitive_constant
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/language-elements/case-transact-sql
case_expression
    : CASE caseExpr=expression switch_section+ (ELSE elseExpr=expression)? END
    | CASE switch_search_condition_section+ (ELSE elseExpr=expression)? END
    ;

unary_operator_expression
    : '~' expression
    | op=('+' | '-') expression
    ;

bracket_expression
    : '(' expression ')' | '(' subquery ')'
    ;

subquery
    : select_statement
    ;

// https://msdn.microsoft.com/en-us/library/ms175972.aspx
with_expression
    : WITH ctes+=common_table_expression (',' ctes+=common_table_expression)*
    ;

common_table_expression
    : expression_name=id_ ('(' columns=column_name_list ')')? AS '(' cte_query=select_statement ')'
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/queries/search-condition-transact-sql
search_condition
    : NOT* (predicate | '(' search_condition ')')
    | search_condition AND search_condition // AND takes precedence over OR
    | search_condition OR search_condition
    ;

predicate
    : EXISTS '(' subquery ')'
    | freetext_predicate
    | expression comparison_operator expression
    | expression MULT_ASSIGN expression ////SQL-82 syntax for left outer joins; '*='. See https://stackoverflow.com/questions/40665/in-sybase-sql
    | expression comparison_operator (ALL | SOME | ANY) '(' subquery ')'
    | expression NOT* BETWEEN expression AND expression
    | expression NOT* IN '(' (subquery | expression_list) ')'
    | expression NOT* LIKE expression (ESCAPE expression)?
    | expression IS null_notnull
    ;

// Changed union rule to sql_union to avoid union construct with C++ target.  Issue reported by person who generates into C++.  This individual reports change causes generated code to work

query_expression
    : query_specification select_order_by_clause? unions+=sql_union* //if using top, order by can be on the "top" side of union :/
    | '(' query_expression ')' (UNION ALL? query_expression)?
    ;

sql_union
    : (UNION ALL? | EXCEPT | INTERSECT) (spec=query_specification | ('(' op=query_expression ')'))
    ;

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
query_specification
    : SELECT allOrDistinct=(ALL | DISTINCT)? top=top_clause?
      columns=select_list
      // https://msdn.microsoft.com/en-us/library/ms188029.aspx
      (INTO into=table_name)?
      (FROM from=table_sources)?
      (WHERE where=search_condition)?
      // https://msdn.microsoft.com/en-us/library/ms177673.aspx
      (GROUP BY ((groupByAll=ALL? groupBys+=group_by_item (',' groupBys+=group_by_item)*) | GROUPING SETS '(' groupSets+=grouping_sets_item (',' groupSets+=grouping_sets_item)* ')'))?
      (HAVING having=search_condition)?
    ;

// https://msdn.microsoft.com/en-us/library/ms189463.aspx
top_clause
    : TOP (top_percent | top_count) (WITH TIES)?
    ;

top_percent
    : percent_constant=(REAL | FLOAT | DECIMAL) PERCENT
    | '(' topper_expression=expression ')' PERCENT
    ;

top_count
    : count_constant=DECIMAL
    | '(' topcount_expression=expression ')'
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/queries/select-over-clause-transact-sql?view=sql-server-ver16
order_by_clause
    : ORDER BY order_bys+=order_by_expression (',' order_bys+=order_by_expression)*
    ;

// https://msdn.microsoft.com/en-us/library/ms188385.aspx
select_order_by_clause
    : order_by_clause
      (OFFSET offset_exp=expression offset_rows=(ROW | ROWS) (FETCH fetch_offset=(FIRST | NEXT) fetch_exp=expression fetch_rows=(ROW | ROWS) ONLY)?)?
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/queries/select-for-clause-transact-sql
for_clause
    : FOR BROWSE
    | FOR XML (RAW ('(' STRING ')')? | AUTO) xml_common_directives*
      (COMMA (XMLDATA | XMLSCHEMA ('(' STRING ')')?))?
      (COMMA ELEMENTS (XSINIL | ABSENT)?)?
    | FOR XML EXPLICIT xml_common_directives*
      (COMMA XMLDATA)?
    | FOR XML PATH ('(' STRING ')')? xml_common_directives*
      (COMMA ELEMENTS (XSINIL | ABSENT)?)?
    | FOR JSON (AUTO | PATH)
      ( COMMA
        ( ROOT ('(' STRING ')')
        | INCLUDE_NULL_VALUES
        | WITHOUT_ARRAY_WRAPPER
        )
      )*
    ;

xml_common_directives
    : ',' (BINARY_KEYWORD BASE64 | TYPE | ROOT ('(' STRING ')')?)
    ;

order_by_expression
    : order_by=expression (ascending=ASC | descending=DESC)?
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/queries/select-group-by-transact-sql?view=sql-server-ver15
grouping_sets_item
    : '('? groupSetItems+=group_by_item (',' groupSetItems+=group_by_item)* ')'?
    | '(' ')'
    ;

group_by_item
    : expression
    /*| rollup_spec
    | cube_spec
    | grouping_sets_spec
    | grand_total*/
    ;

option_clause
    // https://msdn.microsoft.com/en-us/library/ms181714.aspx
    : OPTION '(' options+=option (',' options+=option)* ')'
    ;

option
    : FAST number_rows=DECIMAL
    | (HASH | ORDER) GROUP
    | (MERGE | HASH | CONCAT) UNION
    | (LOOP | MERGE | HASH) JOIN
    | EXPAND VIEWS
    | FORCE ORDER
    | IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX
    | KEEP PLAN
    | KEEPFIXED PLAN
    | MAXDOP number_of_processors=DECIMAL
    | MAXRECURSION number_recursion=DECIMAL
    | OPTIMIZE FOR '(' optimize_for_arg (',' optimize_for_arg)* ')'
    | OPTIMIZE FOR UNKNOWN
    | PARAMETERIZATION (SIMPLE | FORCED)
    | RECOMPILE
    | ROBUST PLAN
    | USE PLAN STRING
    ;

optimize_for_arg
    : LOCAL_ID (UNKNOWN | '=' (constant | NULL_))
    ;

// https://msdn.microsoft.com/en-us/library/ms176104.aspx
select_list
    : selectElement+=select_list_elem (',' selectElement+=select_list_elem)*
    ;

udt_method_arguments
    : '(' argument+=execute_var_string (',' argument+=execute_var_string)* ')'
    ;

// https://docs.microsoft.com/ru-ru/sql/t-sql/queries/select-clause-transact-sql
asterisk
    : (table_name '.')? '*'
    | (INSERTED | DELETED) '.' '*'
    ;

udt_elem
    : udt_column_name=id_ '.' non_static_attr=id_ udt_method_arguments as_column_alias?
    | udt_column_name=id_ DOUBLE_COLON static_attr=id_ udt_method_arguments? as_column_alias?
    ;

expression_elem
    : leftAlias=column_alias eq='=' leftAssignment=expression
    | expressionAs=expression as_column_alias?
    ;

select_list_elem
    : asterisk
    | udt_elem
    | LOCAL_ID (assignment_operator | '=') expression
    | expression_elem
    ;

table_sources
    : non_ansi_join
    | source+=table_source (',' source+=table_source)*
    ;

// https://sqlenlight.com/support/help/sa0006/
non_ansi_join
    : source+=table_source (',' source+=table_source)+
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/queries/from-transact-sql
table_source
    : table_source_item joins+=join_part*
    ;

table_source_item
    : full_table_name             deprecated_table_hint as_table_alias // this is currently allowed
    | full_table_name             as_table_alias? (with_table_hints | deprecated_table_hint | sybase_legacy_hints)?
    | rowset_function             as_table_alias?
    | '(' derived_table ')'       (as_table_alias column_alias_list?)?
    | change_table                as_table_alias?
    | nodes_method                (as_table_alias column_alias_list?)?
    | function_call               (as_table_alias column_alias_list?)?
    | loc_id=LOCAL_ID             as_table_alias?
    | loc_id_call=LOCAL_ID '.' loc_fcall=function_call (as_table_alias column_alias_list?)?
    | open_xml
    | open_json
    | DOUBLE_COLON oldstyle_fcall=function_call       as_table_alias? // Build-in function (old syntax)
    | '(' table_source ')'
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/functions/openxml-transact-sql
open_xml
    : OPENXML '(' expression ',' expression (',' expression)? ')'
    (WITH '(' schema_declaration ')' )? as_table_alias?
    ;

open_json
    : OPENJSON '(' expression (',' expression)? ')'
    (WITH '(' json_declaration ')' )? as_table_alias?
    ;

json_declaration
    : json_col+=json_column_declaration (',' json_col+=json_column_declaration)*
    ;

json_column_declaration
    : column_declaration (AS JSON)?
    ;

schema_declaration
    : xml_col+=column_declaration (',' xml_col+=column_declaration)*
    ;

column_declaration
    : id_ data_type STRING?
    ;

change_table
    : change_table_changes
    | change_table_version
    ;

change_table_changes
    : CHANGETABLE '(' CHANGES changetable=table_name ',' changesid=(NULL_ | DECIMAL | LOCAL_ID) ')'
    ;
change_table_version
    : CHANGETABLE '(' VERSION versiontable=table_name ',' pk_columns=full_column_name_list ',' pk_values=select_list  ')'
    ;

// https://msdn.microsoft.com/en-us/library/ms191472.aspx
join_part
    // https://msdn.microsoft.com/en-us/library/ms173815(v=sql.120).aspx
    : join_on
    | cross_join
    | apply_
    | pivot
    | unpivot
    ;
join_on
    : (inner=INNER? | join_type=(LEFT | RIGHT | FULL) outer=OUTER?) (join_hint=(LOOP | HASH | MERGE | REMOTE))?
       JOIN source=table_source ON cond=search_condition
    ;

cross_join
    : CROSS JOIN table_source_item
    ;

apply_
    : apply_style=(CROSS | OUTER) APPLY source=table_source_item
    ;

pivot
    : PIVOT pivot_clause as_table_alias
    ;

unpivot
    : UNPIVOT unpivot_clause as_table_alias
    ;

pivot_clause
    : '(' aggregate_windowed_function FOR full_column_name IN column_alias_list ')'
    ;

unpivot_clause
    : '(' unpivot_exp=expression FOR full_column_name IN '(' full_column_name_list ')' ')'
    ;

full_column_name_list
    : column+=full_column_name (',' column+=full_column_name)*
    ;

// https://msdn.microsoft.com/en-us/library/ms190312.aspx
rowset_function
    :  (
        OPENROWSET LR_BRACKET provider_name = STRING COMMA connectionString = STRING COMMA sql = STRING RR_BRACKET
     )
     | ( OPENROWSET '(' BULK data_file=STRING ',' (bulk_option (',' bulk_option)* | id_)')' )
    ;

// runtime check.
bulk_option
    : id_ '=' bulk_option_value=(DECIMAL | STRING)
    ;

derived_table
    : subquery
    | '(' subquery (UNION ALL subquery)* ')'
    | table_value_constructor
    | '(' table_value_constructor ')'
    ;

function_call
    : ranking_windowed_function                         #RANKING_WINDOWED_FUNC
    | aggregate_windowed_function                       #AGGREGATE_WINDOWED_FUNC
    | analytic_windowed_function                        #ANALYTIC_WINDOWED_FUNC
    | built_in_functions                                #BUILT_IN_FUNC
    | scalar_function_name '(' expression_list? ')'     #SCALAR_FUNCTION
    | freetext_function                                 #FREE_TEXT
    | partition_function                                #PARTITION_FUNC
    | hierarchyid_static_method                         #HIERARCHYID_METHOD
    ;

partition_function
    : (database=id_ '.')? DOLLAR_PARTITION '.' func_name=id_ '(' expression ')'
    ;

freetext_function
    : (CONTAINSTABLE | FREETEXTTABLE) '(' table_name ',' (full_column_name | '(' full_column_name (',' full_column_name)* ')' | '*' ) ',' expression  (',' LANGUAGE expression)? (',' expression)? ')'
    | (SEMANTICSIMILARITYTABLE | SEMANTICKEYPHRASETABLE) '(' table_name ',' (full_column_name | '(' full_column_name (',' full_column_name)* ')' | '*' ) ',' expression ')'
    | SEMANTICSIMILARITYDETAILSTABLE '(' table_name ',' full_column_name ',' expression ',' full_column_name ',' expression ')'
    ;

freetext_predicate
    : CONTAINS '(' (full_column_name | '(' full_column_name (',' full_column_name)* ')' | '*' | PROPERTY '(' full_column_name ',' expression ')') ',' expression ')'
    | FREETEXT '(' table_name ',' (full_column_name | '(' full_column_name (',' full_column_name)* ')' | '*' ) ',' expression  (',' LANGUAGE expression)? ')'
    ;

json_key_value
    : json_key_name=expression ':' value_expression=expression
    ;

json_null_clause
    : (ABSENT | NULL_) ON NULL_
    ;

built_in_functions
    // Metadata functions
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/app-name-transact-sql?view=sql-server-ver16
    : APP_NAME '(' ')'                                                      #APP_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/applock-mode-transact-sql?view=sql-server-ver16
    | APPLOCK_MODE '(' database_principal=expression ',' resource_name=expression ',' lock_owner=expression ')' #APPLOCK_MODE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/applock-test-transact-sql?view=sql-server-ver16
    | APPLOCK_TEST '(' database_principal=expression ',' resource_name=expression ',' lock_mode=expression ',' lock_owner=expression ')' #APPLOCK_TEST
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/assemblyproperty-transact-sql?view=sql-server-ver16
    | ASSEMBLYPROPERTY '(' assembly_name=expression ',' property_name=expression ')' #ASSEMBLYPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/col-length-transact-sql?view=sql-server-ver16
    | COL_LENGTH '(' table=expression ',' column=expression ')'             #COL_LENGTH
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/col-name-transact-sql?view=sql-server-ver16
    | COL_NAME '(' table_id=expression ',' column_id=expression ')'         #COL_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/columnproperty-transact-sql?view=sql-server-ver16
    | COLUMNPROPERTY '(' id=expression ',' column=expression ',' property=expression ')' #COLUMNPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/databasepropertyex-transact-sql?view=sql-server-ver16
    | DATABASEPROPERTYEX '(' database=expression ',' property=expression ')' #DATABASEPROPERTYEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/db-id-transact-sql?view=sql-server-ver16
    | DB_ID '(' database_name=expression? ')'                               #DB_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/db-name-transact-sql?view=sql-server-ver16
    | DB_NAME '(' database_id=expression? ')'                               #DB_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/file-id-transact-sql?view=sql-server-ver16
    | FILE_ID '(' file_name=expression ')'                                  #FILE_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/file-idex-transact-sql?view=sql-server-ver16
    | FILE_IDEX '(' file_name=expression ')'                                #FILE_IDEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/file-name-transact-sql?view=sql-server-ver16
    | FILE_NAME '(' file_id=expression ')'                                  #FILE_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/filegroup-id-transact-sql?view=sql-server-ver16
    | FILEGROUP_ID '(' filegroup_name=expression ')'                        #FILEGROUP_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/filegroup-name-transact-sql?view=sql-server-ver16
    | FILEGROUP_NAME '(' filegroup_id=expression ')'                        #FILEGROUP_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/filegroupproperty-transact-sql?view=sql-server-ver16
    | FILEGROUPPROPERTY '(' filegroup_name=expression ',' property=expression ')' #FILEGROUPPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/fileproperty-transact-sql?view=sql-server-ver16
    | FILEPROPERTY '(' file_name=expression ',' property=expression ')'     #FILEPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/filepropertyex-transact-sql?view=sql-server-ver16
    | FILEPROPERTYEX '(' name=expression ',' property=expression ')'        #FILEPROPERTYEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/fulltextcatalogproperty-transact-sql?view=sql-server-ver16
    | FULLTEXTCATALOGPROPERTY '(' catalog_name=expression ',' property=expression ')' #FULLTEXTCATALOGPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/fulltextserviceproperty-transact-sql?view=sql-server-ver16
    | FULLTEXTSERVICEPROPERTY '(' property=expression ')'                   #FULLTEXTSERVICEPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/index-col-transact-sql?view=sql-server-ver16
    | INDEX_COL '(' table_or_view_name=expression ',' index_id=expression ',' key_id=expression ')' #INDEX_COL
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/indexkey-property-transact-sql?view=sql-server-ver16
    | INDEXKEY_PROPERTY '(' object_id=expression ',' index_id=expression ',' key_id=expression ',' property=expression ')' #INDEXKEY_PROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/indexproperty-transact-sql?view=sql-server-ver16
    | INDEXPROPERTY '(' object_id=expression ',' index_or_statistics_name=expression ',' property=expression ')' #INDEXPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/next-value-for-transact-sql?view=sql-server-ver16
    | NEXT VALUE FOR sequence_name=table_name ( OVER '(' order_by_clause ')' )? #NEXT_VALUE_FOR
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/object-definition-transact-sql?view=sql-server-ver16
    | OBJECT_DEFINITION '(' object_id=expression ')'                        #OBJECT_DEFINITION
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/object-id-transact-sql?view=sql-server-ver16
    | OBJECT_ID '(' object_name=expression ( ',' object_type=expression )? ')'      #OBJECT_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/object-name-transact-sql?view=sql-server-ver16
    | OBJECT_NAME '(' object_id=expression ( ',' database_id=expression )? ')' #OBJECT_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/object-schema-name-transact-sql?view=sql-server-ver16
    | OBJECT_SCHEMA_NAME '(' object_id=expression ( ',' database_id=expression )? ')' #OBJECT_SCHEMA_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/objectproperty-transact-sql?view=sql-server-ver16
    | OBJECTPROPERTY '(' id=expression ',' property=expression ')'          #OBJECTPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/objectpropertyex-transact-sql?view=sql-server-ver16
    | OBJECTPROPERTYEX '(' id=expression ',' property=expression ')'        #OBJECTPROPERTYEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/original-db-name-transact-sql?view=sql-server-ver16
    | ORIGINAL_DB_NAME '(' ')'                                              #ORIGINAL_DB_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/parsename-transact-sql?view=sql-server-ver16
    | PARSENAME '(' object_name=expression ',' object_piece=expression ')'  #PARSENAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-id-transact-sql?view=sql-server-ver16
    | SCHEMA_ID '(' schema_name=expression? ')'                             #SCHEMA_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/schema-name-transact-sql?view=sql-server-ver16
    | SCHEMA_NAME '(' schema_id=expression? ')'                             #SCHEMA_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/scope-identity-transact-sql?view=sql-server-ver16
    | SCOPE_IDENTITY '(' ')'                                                #SCOPE_IDENTITY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/serverproperty-transact-sql?view=sql-server-ver16
    | SERVERPROPERTY '(' property=expression ')'                            #SERVERPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/stats-date-transact-sql?view=sql-server-ver16
    | STATS_DATE '(' object_id=expression ',' stats_id=expression ')'       #STATS_DATE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/type-id-transact-sql?view=sql-server-ver16
    | TYPE_ID '(' type_name=expression ')'                                  #TYPE_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/type-name-transact-sql?view=sql-server-ver16
    | TYPE_NAME '(' type_id=expression ')'                                  #TYPE_NAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/typeproperty-transact-sql?view=sql-server-ver16
    | TYPEPROPERTY '(' type=expression ',' property=expression ')'          #TYPEPROPERTY
    // String functions
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/ascii-transact-sql?view=sql-server-ver16
    | ASCII '(' character_expression=expression ')'                         #ASCII
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/char-transact-sql?view=sql-server-ver16
    | CHAR '(' integer_expression=expression ')'                            #CHAR
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/charindex-transact-sql?view=sql-server-ver16
    | CHARINDEX '(' expressionToFind=expression ',' expressionToSearch=expression ( ',' start_location=expression )? ')' #CHARINDEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/concat-transact-sql?view=sql-server-ver16
    | CONCAT '(' string_value_1=expression ',' string_value_2=expression ( ',' string_value_n+=expression )* ')' #CONCAT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/concat-ws-transact-sql?view=sql-server-ver16
    | CONCAT_WS '(' separator=expression ',' argument_1=expression ',' argument_2=expression ( ',' argument_n+=expression )* ')' #CONCAT_WS
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/difference-transact-sql?view=sql-server-ver16
    | DIFFERENCE '(' character_expression_1=expression ',' character_expression_2=expression ')' #DIFFERENCE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/format-transact-sql?view=sql-server-ver16
    | FORMAT '(' value=expression ',' format=expression ( ',' culture=expression )? ')' #FORMAT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/left-transact-sql?view=sql-server-ver16
    | LEFT '(' character_expression=expression ',' integer_expression=expression ')' #LEFT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/len-transact-sql?view=sql-server-ver16
    | LEN '(' string_expression=expression ')'                              #LEN
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/lower-transact-sql?view=sql-server-ver16
    | LOWER '(' character_expression=expression ')'                         #LOWER
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/ltrim-transact-sql?view=sql-server-ver16
    | LTRIM '(' character_expression=expression ')'                         #LTRIM
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/nchar-transact-sql?view=sql-server-ver16
    | NCHAR '(' integer_expression=expression ')'                           #NCHAR
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/patindex-transact-sql?view=sql-server-ver16
    | PATINDEX '(' pattern=expression ',' string_expression=expression ')'  #PATINDEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/quotename-transact-sql?view=sql-server-ver16
    | QUOTENAME '(' character_string=expression ( ',' quote_character=expression )? ')' #QUOTENAME
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/replace-transact-sql?view=sql-server-ver16
    | REPLACE '(' input=expression ',' replacing=expression ',' with=expression ')'   #REPLACE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/replicate-transact-sql?view=sql-server-ver16
    | REPLICATE '(' string_expression=expression ',' integer_expression=expression ')' #REPLICATE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/reverse-transact-sql?view=sql-server-ver16
    | REVERSE '(' string_expression=expression ')'                          #REVERSE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/right-transact-sql?view=sql-server-ver16
    | RIGHT '(' character_expression=expression ',' integer_expression=expression ')' #RIGHT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/rtrim-transact-sql?view=sql-server-ver16
    | RTRIM '(' character_expression=expression ')'                         #RTRIM
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/soundex-transact-sql?view=sql-server-ver16
    | SOUNDEX '(' character_expression=expression ')'                       #SOUNDEX
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/space-transact-sql?view=sql-server-ver16
    | SPACE_KEYWORD '(' integer_expression=expression ')'                   #SPACE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/str-transact-sql?view=sql-server-ver16
    | STR '(' float_expression=expression ( ',' length_expression=expression ( ',' decimal=expression )? )? ')' #STR
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/string-agg-transact-sql?view=sql-server-ver16
    | STRING_AGG '(' expr=expression ',' separator=expression ')' (WITHIN GROUP '(' order_by_clause ')')?  #STRINGAGG
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/string-escape-transact-sql?view=sql-server-ver16
    | STRING_ESCAPE '(' text_=expression ',' type_=expression ')'           #STRING_ESCAPE
    // https://msdn.microsoft.com/fr-fr/library/ms188043.aspx
    | STUFF '(' str=expression ',' from=expression ',' to=expression ',' str_with=expression ')' #STUFF
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/substring-transact-sql?view=sql-server-ver16
    | SUBSTRING '(' string_expression=expression ',' start_=expression ',' length=expression ')' #SUBSTRING
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/translate-transact-sql?view=sql-server-ver16
    | TRANSLATE '(' inputString=expression ',' characters=expression ',' translations=expression ')' #TRANSLATE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/trim-transact-sql?view=sql-server-ver16
    | TRIM '(' ( characters=expression FROM )? string_=expression ')'       #TRIM
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/unicode-transact-sql?view=sql-server-ver16
    | UNICODE '(' ncharacter_expression=expression ')'                      #UNICODE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/upper-transact-sql?view=sql-server-ver16
    | UPPER '(' character_expression=expression ')'                         #UPPER
    // System functions
    // https://msdn.microsoft.com/en-us/library/ms173784.aspx
    | BINARY_CHECKSUM '(' ( star='*' | expression (',' expression)* ) ')'   #BINARY_CHECKSUM
    // https://msdn.microsoft.com/en-us/library/ms189788.aspx
    | CHECKSUM '(' ( star='*' | expression (',' expression)* ) ')'          #CHECKSUM
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/compress-transact-sql?view=sql-server-ver16
    | COMPRESS '(' expr=expression ')'                                      #COMPRESS
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/connectionproperty-transact-sql?view=sql-server-ver16
    | CONNECTIONPROPERTY '(' property=STRING ')'                            #CONNECTIONPROPERTY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/context-info-transact-sql?view=sql-server-ver16
    | CONTEXT_INFO '(' ')'                                                  #CONTEXT_INFO
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/current-request-id-transact-sql?view=sql-server-ver16
    | CURRENT_REQUEST_ID '(' ')'                                            #CURRENT_REQUEST_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/current-transaction-id-transact-sql?view=sql-server-ver16
    | CURRENT_TRANSACTION_ID '(' ')'                                        #CURRENT_TRANSACTION_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/decompress-transact-sql?view=sql-server-ver16
    | DECOMPRESS '(' expr=expression ')'                                    #DECOMPRESS
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-line-transact-sql?view=sql-server-ver16
    | ERROR_LINE '(' ')'                                                    #ERROR_LINE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-message-transact-sql?view=sql-server-ver16
    | ERROR_MESSAGE '(' ')'                                                 #ERROR_MESSAGE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-number-transact-sql?view=sql-server-ver16
    | ERROR_NUMBER '(' ')'                                                  #ERROR_NUMBER
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-procedure-transact-sql?view=sql-server-ver16
    | ERROR_PROCEDURE '(' ')'                                               #ERROR_PROCEDURE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-severity-transact-sql?view=sql-server-ver16
    | ERROR_SEVERITY '(' ')'                                                #ERROR_SEVERITY
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/error-state-transact-sql?view=sql-server-ver16
    | ERROR_STATE '(' ')'                                                   #ERROR_STATE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/formatmessage-transact-sql?view=sql-server-ver16
    | FORMATMESSAGE '(' (msg_number=DECIMAL | msg_string=STRING | msg_variable=LOCAL_ID) ',' expression (',' expression)* ')' #FORMATMESSAGE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/get-filestream-transaction-context-transact-sql?view=sql-server-ver16
    | GET_FILESTREAM_TRANSACTION_CONTEXT '(' ')'                            #GET_FILESTREAM_TRANSACTION_CONTEXT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/getansinull-transact-sql?view=sql-server-ver16
    | GETANSINULL '(' (database=STRING)? ')'                                #GETANSINULL
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/host-id-transact-sql?view=sql-server-ver16
    | HOST_ID '(' ')'                                                       #HOST_ID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/host-name-transact-sql?view=sql-server-ver16
    | HOST_NAME '(' ')'                                                     #HOST_NAME
    // https://msdn.microsoft.com/en-us/library/ms184325.aspx
    | ISNULL '(' left=expression ',' right=expression ')'                   #ISNULL
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/isnumeric-transact-sql?view=sql-server-ver16
    | ISNUMERIC '(' expression ')'                                          #ISNUMERIC
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/min-active-rowversion-transact-sql?view=sql-server-ver16
    | MIN_ACTIVE_ROWVERSION '(' ')'                                         #MIN_ACTIVE_ROWVERSION
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/newid-transact-sql?view=sql-server-ver16
    | NEWID '(' ')'                                                         #NEWID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/newsequentialid-transact-sql?view=sql-server-ver16
    | NEWSEQUENTIALID '(' ')'                                               #NEWSEQUENTIALID
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/rowcount-big-transact-sql?view=sql-server-ver16
    | ROWCOUNT_BIG '(' ')'                                                  #ROWCOUNT_BIG
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/session-context-transact-sql?view=sql-server-ver16
    | SESSION_CONTEXT '(' key=STRING ')'                                    #SESSION_CONTEXT
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/xact-state-transact-sql?view=sql-server-ver16
    | XACT_STATE '(' ')'                                                    #XACT_STATE
    // https://msdn.microsoft.com/en-us/library/hh231076.aspx
    // https://msdn.microsoft.com/en-us/library/ms187928.aspx
    | CAST '(' expression AS data_type ')'              #CAST
    | TRY_CAST '(' expression AS data_type ')'          #TRY_CAST
    | CONVERT '(' convert_data_type=data_type ','convert_expression=expression (',' style=expression)? ')'                              #CONVERT
    // https://msdn.microsoft.com/en-us/library/ms190349.aspx
    | COALESCE '(' expression_list ')'                  #COALESCE
    // Cursor functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cursor-rows-transact-sql?view=sql-server-ver16
    | CURSOR_ROWS                                       #CURSOR_ROWS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cursor-rows-transact-sql?view=sql-server-ver16
    | FETCH_STATUS                                      #FETCH_STATUS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cursor-status-transact-sql?view=sql-server-ver16
    | CURSOR_STATUS '(' scope=STRING ',' cursor=expression ')' #CURSOR_STATUS
    // Cryptographic functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cert-id-transact-sql?view=sql-server-ver16
    | CERT_ID '(' cert_name=expression ')'              #CERT_ID
    // Data type functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datalength-transact-sql?view=sql-server-ver16
    | DATALENGTH '(' expression ')'                     #DATALENGTH
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ident-current-transact-sql?view=sql-server-ver16
    | IDENT_CURRENT '(' table_or_view=expression ')'    # IDENT_CURRENT
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ident-incr-transact-sql?view=sql-server-ver16
    | IDENT_INCR '(' table_or_view=expression ')'       # IDENT_INCR
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ident-seed-transact-sql?view=sql-server-ver16
    | IDENT_SEED '(' table_or_view=expression ')'       # IDENT_SEED
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ident-seed-transact-sql?view=sql-server-ver16
    | IDENTITY '(' datatype=data_type (',' seed=DECIMAL ',' increment=DECIMAL)? ')' #IDENTITY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ident-seed-transact-sql?view=sql-server-ver16
    | SQL_VARIANT_PROPERTY '(' expr=expression ',' property=STRING ')' #SQL_VARIANT_PROPERTY
    // Date functions
    //https://infocenter.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc36271.1572/html/blocks/CJADIDHD.htm
    | CURRENT_DATE '(' ')'                              #CURRENT_DATE
    // https://msdn.microsoft.com/en-us/library/ms188751.aspx
    | CURRENT_TIMESTAMP                                 #CURRENT_TIMESTAMP
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/current-timezone-transact-sql?view=sql-server-ver16
    | CURRENT_TIMEZONE '(' ')'                          #CURRENT_TIMEZONE
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/current-timezone-id-transact-sql?view=sql-server-ver16
    | CURRENT_TIMEZONE_ID '(' ')'                       #CURRENT_TIMEZONE_ID
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/date-bucket-transact-sql?view=sql-server-ver16
    | DATE_BUCKET '(' datepart=dateparts_9 ',' number=expression ',' date=expression (',' origin=expression)? ')' #DATE_BUCKET
    // https://msdn.microsoft.com/en-us/library/ms186819.aspx
    | DATEADD '(' datepart=dateparts_12 ',' number=expression ',' date=expression ')'  #DATEADD
    // https://msdn.microsoft.com/en-us/library/ms189794.aspx
    | DATEDIFF '(' datepart=dateparts_12 ',' date_first=expression ',' date_second=expression ')' #DATEDIFF
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datediff-big-transact-sql?view=sql-server-ver16
    | DATEDIFF_BIG '(' datepart=dateparts_12 ',' startdate=expression ',' enddate=expression ')' #DATEDIFF_BIG
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datefromparts-transact-sql?view=sql-server-ver16
    | DATEFROMPARTS '(' year=expression ',' month=expression ',' day=expression ')'#DATEFROMPARTS
    // https://msdn.microsoft.com/en-us/library/ms174395.aspx
    | DATENAME '(' datepart=dateparts_15 ',' date=expression ')'                #DATENAME
    // https://msdn.microsoft.com/en-us/library/ms174420.aspx
    | DATEPART '(' datepart=dateparts_15 ',' date=expression ')'                #DATEPART
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datetime2fromparts-transact-sql?view=sql-server-ver16
    | DATETIME2FROMPARTS '(' year=expression ',' month=expression ',' day=expression ',' hour=expression ',' minute=expression ',' seconds=expression ',' fractions=expression ',' precision=expression ')' #DATETIME2FROMPARTS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datetimefromparts-transact-sql?view=sql-server-ver16
    | DATETIMEFROMPARTS '(' year=expression ',' month=expression ',' day=expression ',' hour=expression ',' minute=expression ',' seconds=expression ',' milliseconds=expression ')' #DATETIMEFROMPARTS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datetimeoffsetfromparts-transact-sql?view=sql-server-ver16
    | DATETIMEOFFSETFROMPARTS '(' year=expression ',' month=expression ',' day=expression ',' hour=expression ',' minute=expression ',' seconds=expression ',' fractions=expression ',' hour_offset=expression ',' minute_offset=expression ',' precision=DECIMAL ')' #DATETIMEOFFSETFROMPARTS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/datetrunc-transact-sql?view=sql-server-ver16
    | DATETRUNC '(' datepart=dateparts_datetrunc ',' date=expression ')' #DATETRUNC
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/day-transact-sql?view=sql-server-ver16
    | DAY '(' date=expression ')' #DAY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/eomonth-transact-sql?view=sql-server-ver16
    | EOMONTH '(' start_date=expression (',' month_to_add=expression)? ')'#EOMONTH
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/getdate-transact-sql
    | GETDATE '(' ')'                                   #GETDATE
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/getdate-transact-sql
    | GETUTCDATE '(' ')'                                #GETUTCDATE
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/isdate-transact-sql?view=sql-server-ver16
    | ISDATE '(' expression ')' #ISDATE
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/month-transact-sql?view=sql-server-ver16
    | MONTH '(' date=expression ')' #MONTH
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/smalldatetimefromparts-transact-sql?view=sql-server-ver16
    | SMALLDATETIMEFROMPARTS '(' year=expression ',' month=expression ',' day=expression ',' hour=expression ',' minute=expression ')' #SMALLDATETIMEFROMPARTS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/switchoffset-transact-sql?view=sql-server-ver16
    | SWITCHOFFSET '(' datetimeoffset_expression=expression ',' timezoneoffset_expression=expression ')' #SWITCHOFFSET
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sysdatetime-transact-sql?view=sql-server-ver16
    | SYSDATETIME '(' ')' #SYSDATETIME
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sysdatetimeoffset-transact-sql?view=sql-server-ver16
    | SYSDATETIMEOFFSET '(' ')' #SYSDATETIMEOFFSET
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sysutcdatetime-transact-sql?view=sql-server-ver16
    | SYSUTCDATETIME '(' ')' #SYSUTCDATETIME
    //https://learn.microsoft.com/en-us/sql/t-sql/functions/timefromparts-transact-sql?view=sql-server-ver16
    | TIMEFROMPARTS '(' hour=expression ',' minute=expression ',' seconds=expression ',' fractions=expression ',' precision=DECIMAL ')' #TIMEFROMPARTS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/todatetimeoffset-transact-sql?view=sql-server-ver16
    | TODATETIMEOFFSET '(' datetime_expression=expression ',' timezoneoffset_expression=expression ')' #TODATETIMEOFFSET
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/year-transact-sql?view=sql-server-ver16
    | YEAR '(' date=expression ')' #YEAR
    // https://msdn.microsoft.com/en-us/library/ms189838.aspx
    | IDENTITY '(' data_type (',' seed=DECIMAL)? (',' increment=DECIMAL)? ')'                                                           #IDENTITY
    // https://msdn.microsoft.com/en-us/library/bb839514.aspx
    | MIN_ACTIVE_ROWVERSION '(' ')'                     #MIN_ACTIVE_ROWVERSION
    // https://msdn.microsoft.com/en-us/library/ms177562.aspx
    | NULLIF '(' left=expression ',' right=expression ')'          #NULLIF
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/parse-transact-sql
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/try-parse-transact-sql
    | PARSE '(' str=expression AS data_type ( USING culture=expression )? ')'          #PARSE
    // https://docs.microsoft.com/en-us/sql/t-sql/xml/xml-data-type-methods
    | xml_data_type_methods                             #XML_DATA_TYPE_FUNC
    // https://docs.microsoft.com/en-us/sql/t-sql/functions/logical-functions-iif-transact-sql
    | IIF '(' cond=search_condition ',' left=expression ',' right=expression ')'   #IIF
    // JSON functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/isjson-transact-sql?view=azure-sqldw-latest
    | ISJSON '(' json_expr=expression (',' json_type_constraint=expression)? ')' #ISJSON
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-object-transact-sql?view=azure-sqldw-latest
    | JSON_OBJECT '(' (key_value=json_key_value (',' key_value=json_key_value)*)? json_null_clause? ')' #JSON_OBJECT
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-array-transact-sql?view=azure-sqldw-latest
    | JSON_ARRAY '(' expression_list? json_null_clause? ')' #JSON_ARRAY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-value-transact-sql?view=azure-sqldw-latest
    | JSON_VALUE '(' expr=expression ',' path=expression ')' #JSON_VALUE
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-query-transact-sql?view=azure-sqldw-latest
    | JSON_QUERY '(' expr=expression (',' path=expression)? ')' #JSON_QUERY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-modify-transact-sql?view=azure-sqldw-latest
    | JSON_MODIFY '(' expr=expression ',' path=expression ',' new_value=expression ')' #JSON_MODIFY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/json-path-exists-transact-sql?view=azure-sqldw-latest
    | JSON_PATH_EXISTS '(' value_expression=expression ',' sql_json_path=expression ')' #JSON_PATH_EXISTS
    // Math functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/abs-transact-sql?view=sql-server-ver16
    | ABS '(' numeric_expression=expression ')' #ABS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/acos-transact-sql?view=sql-server-ver16
    | ACOS '(' float_expression=expression ')' #ACOS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/asin-transact-sql?view=sql-server-ver16
    | ASIN '(' float_expression=expression ')' #ASIN
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/atan-transact-sql?view=sql-server-ver16
    | ATAN '(' float_expression=expression ')' #ATAN
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/atn2-transact-sql?view=sql-server-ver16
    | ATN2 '(' float_expression=expression ',' float_expression=expression ')' #ATN2
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/ceiling-transact-sql?view=sql-server-ver16
    | CEILING '(' numeric_expression=expression ')' #CEILING
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cos-transact-sql?view=sql-server-ver16
    | COS '(' float_expression=expression ')' #COS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/cot-transact-sql?view=sql-server-ver16
    | COT '(' float_expression=expression ')' #COT
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/degrees-transact-sql?view=sql-server-ver16
    | DEGREES '(' numeric_expression=expression ')' #DEGREES
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/exp-transact-sql?view=sql-server-ver16
    | EXP '(' float_expression=expression ')' #EXP
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/floor-transact-sql?view=sql-server-ver16
    | FLOOR '(' numeric_expression=expression ')' #FLOOR
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/log-transact-sql?view=sql-server-ver16
    | LOG '(' float_expression=expression (',' base=expression)? ')' #LOG
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/log10-transact-sql?view=sql-server-ver16
    | LOG10 '(' float_expression=expression ')' #LOG10
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/pi-transact-sql?view=sql-server-ver16
    | PI '(' ')' #PI
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/power-transact-sql?view=sql-server-ver16
    | POWER '(' float_expression=expression ',' y=expression ')' #POWER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/radians-transact-sql?view=sql-server-ver16
    | RADIANS '(' numeric_expression=expression ')' #RADIANS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/rand-transact-sql?view=sql-server-ver16
    | RAND '(' (seed=expression)? ')' #RAND
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/round-transact-sql?view=sql-server-ver16
    | ROUND '(' numeric_expression=expression ',' length=expression (',' function=expression)? ')' #ROUND
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sign-transact-sql?view=sql-server-ver16
    | SIGN '(' numeric_expression=expression ')' #MATH_SIGN
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sin-transact-sql?view=sql-server-ver16
    | SIN '(' float_expression=expression ')' #SIN
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sqrt-transact-sql?view=sql-server-ver16
    | SQRT '(' float_expression=expression ')' #SQRT
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/square-transact-sql?view=sql-server-ver16
    | SQUARE '(' float_expression=expression ')' #SQUARE
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/tan-transact-sql?view=sql-server-ver16
    | TAN '(' float_expression=expression ')' #TAN
    // Logical functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/logical-functions-greatest-transact-sql?view=azure-sqldw-latest
    | GREATEST '(' expression_list ')' #GREATEST
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/logical-functions-least-transact-sql?view=azure-sqldw-latest
    | LEAST '(' expression_list ')' #LEAST
    // Security functions
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/certencoded-transact-sql?view=sql-server-ver16
    | CERTENCODED '(' certid=expression ')'             #CERTENCODED
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/certprivatekey-transact-sql?view=sql-server-ver16
    | CERTPRIVATEKEY '(' certid=expression ',' encryption_password=expression (',' decryption_pasword=expression)? ')' #CERTPRIVATEKEY
    // https://msdn.microsoft.com/en-us/library/ms176050.aspx
    | CURRENT_USER                                      #CURRENT_USER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/database-principal-id-transact-sql?view=sql-server-ver16
    | DATABASE_PRINCIPAL_ID '(' (principal_name=expression)? ')' #DATABASE_PRINCIPAL_ID
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/has-dbaccess-transact-sql?view=sql-server-ver16
    | HAS_DBACCESS '(' database_name=expression ')'     #HAS_DBACCESS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/has-perms-by-name-transact-sql?view=sql-server-ver16
    | HAS_PERMS_BY_NAME '(' securable=expression ',' securable_class=expression ',' permission=expression ( ',' sub_securable=expression (',' sub_securable_class=expression )? )? ')' #HAS_PERMS_BY_NAME
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/is-member-transact-sql?view=sql-server-ver16
    | IS_MEMBER '(' group_or_role=expression ')'        #IS_MEMBER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/is-rolemember-transact-sql?view=sql-server-ver16
    | IS_ROLEMEMBER '(' role=expression ( ',' database_principal=expression )? ')' #IS_ROLEMEMBER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/is-srvrolemember-transact-sql?view=sql-server-ver16
    | IS_SRVROLEMEMBER '(' role=expression ( ',' login=expression )? ')' #IS_SRVROLEMEMBER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/loginproperty-transact-sql?view=sql-server-ver16
    | LOGINPROPERTY '(' login_name=expression ',' property_name=expression ')' #LOGINPROPERTY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/original-login-transact-sql?view=sql-server-ver16
    | ORIGINAL_LOGIN '(' ')'                            #ORIGINAL_LOGIN
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/permissions-transact-sql?view=sql-server-ver16
    | PERMISSIONS '(' ( object_id=expression (',' column=expression)? )? ')' #PERMISSIONS
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/pwdencrypt-transact-sql?view=sql-server-ver16
    | PWDENCRYPT '(' password=expression ')'            #PWDENCRYPT
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/pwdcompare-transact-sql?view=sql-server-ver16
    | PWDCOMPARE '(' clear_text_password=expression ',' password_hash=expression (',' version=expression )?')' #PWDCOMPARE
    // https://msdn.microsoft.com/en-us/library/ms177587.aspx
    | SESSION_USER                                      #SESSION_USER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/sessionproperty-transact-sql?view=sql-server-ver16
    | SESSIONPROPERTY '(' option_name=expression ')'    #SESSIONPROPERTY
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/suser-id-transact-sql?view=sql-server-ver16
    | SUSER_ID '(' (login=expression)? ')'              #SUSER_ID
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/suser-name-transact-sql?view=sql-server-ver16
    | SUSER_NAME '(' (server_user_sid=expression)? ')'  #SUSER_SNAME
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/suser-sid-transact-sql?view=sql-server-ver16
    | SUSER_SID '(' (login=expression (',' param2=expression)?)? ')' #SUSER_SID
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/suser-sname-transact-sql?view=sql-server-ver16
    | SUSER_SNAME '(' (server_user_sid=expression)? ')' #SUSER_SNAME
    // https://msdn.microsoft.com/en-us/library/ms179930.aspx
    | SYSTEM_USER                                       #SYSTEM_USER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/user-transact-sql?view=sql-server-ver16
    | USER                                              #USER
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/user-id-transact-sql?view=sql-server-ver16
    | USER_ID '(' (user=expression)? ')'                #USER_ID
    // https://learn.microsoft.com/en-us/sql/t-sql/functions/user-name-transact-sql?view=sql-server-ver16
    | USER_NAME '(' (id=expression)? ')' #USER_NAME
    ;

xml_data_type_methods
    : value_method
    | query_method
    | exist_method
    | modify_method
    ;

// https://learn.microsoft.com/en-us/sql/t-sql/functions/date-bucket-transact-sql?view=sql-server-ver16
dateparts_9
    : YEAR | YEAR_ABBR
    | QUARTER | QUARTER_ABBR
    | MONTH | MONTH_ABBR
    | DAY | DAY_ABBR
    | WEEK | WEEK_ABBR
    | HOUR | HOUR_ABBR
    | MINUTE | MINUTE_ABBR
    | SECOND | SECOND_ABBR
    | MILLISECOND | MILLISECOND_ABBR
    ;

// https://learn.microsoft.com/en-us/sql/t-sql/functions/dateadd-transact-sql?view=sql-server-ver16
dateparts_12
    : dateparts_9
    | DAYOFYEAR | DAYOFYEAR_ABBR
    | MICROSECOND | MICROSECOND_ABBR
    | NANOSECOND | NANOSECOND_ABBR
    ;

// https://learn.microsoft.com/en-us/sql/t-sql/functions/datename-transact-sql?view=sql-server-ver16
dateparts_15
    : dateparts_12
    | WEEKDAY | WEEKDAY_ABBR
    | TZOFFSET | TZOFFSET_ABBR
    | ISO_WEEK | ISO_WEEK_ABBR
    ;

// https://learn.microsoft.com/en-us/sql/t-sql/functions/datetrunc-transact-sql?view=sql-server-ver16
dateparts_datetrunc
    : dateparts_9
    | DAYOFYEAR | DAYOFYEAR_ABBR
    | MICROSECOND | MICROSECOND_ABBR
    | ISO_WEEK | ISO_WEEK_ABBR
    ;

value_method
    : (loc_id=LOCAL_ID | value_id=full_column_name | eventdata=EVENTDATA '(' ')'  | query=query_method | '(' subquery ')') '.' call=value_call
    ;

value_call
    :  (VALUE | VALUE_SQUARE_BRACKET) '(' xquery=STRING ',' sqltype=STRING ')'
    ;

query_method
    : (loc_id=LOCAL_ID | value_id=full_column_name | '(' subquery ')' ) '.' call=query_call
    ;

query_call
    : (QUERY | QUERY_SQUARE_BRACKET) '(' xquery=STRING ')'
    ;

exist_method
    : (loc_id=LOCAL_ID | value_id=full_column_name | '(' subquery ')') '.' call=exist_call
    ;

exist_call
    : (EXIST | EXIST_SQUARE_BRACKET) '(' xquery=STRING ')'
    ;

modify_method
    : (loc_id=LOCAL_ID | value_id=full_column_name | '(' subquery ')') '.' call=modify_call
    ;

modify_call
    : (MODIFY | MODIFY_SQUARE_BRACKET) '(' xml_dml=STRING ')'
    ;

hierarchyid_call
    : GETANCESTOR '(' n=expression ')'
    | GETDESCENDANT '(' child1=expression ',' child2=expression ')'
    | GETLEVEL '(' ')'
    | ISDESCENDANTOF '(' parent_=expression ')'
    | GETREPARENTEDVALUE '(' oldroot=expression ',' newroot=expression ')'
    | TOSTRING '(' ')'
    ;

hierarchyid_static_method
    : HIERARCHYID DOUBLE_COLON (GETROOT '(' ')' | PARSE '(' input=expression ')')
    ;

nodes_method
    : (loc_id=LOCAL_ID | value_id=full_column_name | '(' subquery ')') '.' NODES '(' xquery=STRING ')'
    ;


switch_section
    : WHEN expression THEN expression
    ;

switch_search_condition_section
    : WHEN search_condition THEN expression
    ;

as_column_alias
    : AS? column_alias
    ;

as_table_alias
    : AS? table_alias
    ;

table_alias
    : id_
    ;

// https://msdn.microsoft.com/en-us/library/ms187373.aspx
with_table_hints
    : WITH '(' hint+=table_hint (','? hint+=table_hint)* ')'
    ;

deprecated_table_hint
    : '(' table_hint ')'
    ;

// https://infocenter-archive.sybase.com/help/index.jsp?topic=/com.sybase.infocenter.dc00938.1502/html/locking/locking103.htm
// https://infocenter-archive.sybase.com/help/index.jsp?topic=/com.sybase.dc32300_1250/html/sqlug/sqlug792.htm
// https://infocenter-archive.sybase.com/help/index.jsp?topic=/com.sybase.dc36271_36272_36273_36274_1250/html/refman/X35229.htm
// Legacy hint with no parenthesis and no WITH keyword. Actually conflicts with table alias name except for holdlock which is
// a reserved keyword in this grammar. We might want a separate sybase grammar variant.
sybase_legacy_hints
    : sybase_legacy_hint+
    ;

sybase_legacy_hint
    : HOLDLOCK
    | NOHOLDLOCK
    | READPAST
    | SHARED
    ;

// For simplicity, we don't build subsets for INSERT/UPDATE/DELETE/SELECT/MERGE
// which means the grammar accept slightly more than the what the specification (documentation) says.
table_hint
    : NOEXPAND
    | INDEX (
            '(' index_value (',' index_value)* ')'
            | '=' '(' index_value ')'
            | '=' index_value // examples in the doc include this syntax
            )
    | FORCESEEK ( '(' index_value '(' column_name_list ')' ')' )?
    | FORCESCAN
    | HOLDLOCK
    | NOLOCK
    | NOWAIT
    | PAGLOCK
    | READCOMMITTED
    | READCOMMITTEDLOCK
    | READPAST
    | READUNCOMMITTED
    | REPEATABLEREAD
    | ROWLOCK
    | SERIALIZABLE
    | SNAPSHOT
    | SPATIAL_WINDOW_MAX_CELLS '=' DECIMAL
    | TABLOCK
    | TABLOCKX
    | UPDLOCK
    | XLOCK
    | KEEPIDENTITY
    | KEEPDEFAULTS
    | IGNORE_CONSTRAINTS
    | IGNORE_TRIGGERS
    ;

index_value
    : id_ | DECIMAL
    ;

column_alias_list
    : '(' alias+=column_alias (',' alias+=column_alias)* ')'
    ;

column_alias
    : id_
    | STRING
    ;

table_value_constructor
    : VALUES '(' exps+=expression_list ')' (',' '(' exps+=expression_list ')')*
    ;

expression_list
    : exp+=expression (',' exp+=expression)*
    ;

// https://msdn.microsoft.com/en-us/library/ms189798.aspx
ranking_windowed_function
    : (RANK | DENSE_RANK | ROW_NUMBER) '(' ')' over_clause
    | NTILE '(' expression ')' over_clause
    ;

// https://msdn.microsoft.com/en-us/library/ms173454.aspx
aggregate_windowed_function
    : agg_func=(AVG | MAX | MIN | SUM | STDEV | STDEVP | VAR | VARP)
      '(' all_distinct_expression ')' over_clause?
    | cnt=(COUNT | COUNT_BIG)
      '(' ('*' | all_distinct_expression) ')' over_clause?
    | CHECKSUM_AGG '(' all_distinct_expression ')'
    | GROUPING '(' expression ')'
    | GROUPING_ID '(' expression_list ')'
    ;

// https://docs.microsoft.com/en-us/sql/t-sql/functions/analytic-functions-transact-sql
analytic_windowed_function
    : (FIRST_VALUE | LAST_VALUE) '(' expression ')' over_clause
    | (LAG | LEAD) '(' expression  (',' expression (',' expression)? )? ')' over_clause
    | (CUME_DIST | PERCENT_RANK) '(' ')' OVER '(' (PARTITION BY expression_list)? order_by_clause ')'
    | (PERCENTILE_CONT | PERCENTILE_DISC) '(' expression ')' WITHIN GROUP '(' order_by_clause ')' OVER '(' (PARTITION BY expression_list)? ')'
    ;

all_distinct_expression
    : (ALL | DISTINCT)? expression
    ;

// https://msdn.microsoft.com/en-us/library/ms189461.aspx
over_clause
    : OVER '(' (PARTITION BY expression_list)? order_by_clause? row_or_range_clause? ')'
    ;

row_or_range_clause
    : (ROWS | RANGE) window_frame_extent
    ;

window_frame_extent
    : window_frame_preceding
    | BETWEEN window_frame_bound AND window_frame_bound
    ;

window_frame_bound
    : window_frame_preceding
    | window_frame_following
    ;

window_frame_preceding
    : UNBOUNDED PRECEDING
    | DECIMAL PRECEDING
    | CURRENT ROW
    ;

window_frame_following
    : UNBOUNDED FOLLOWING
    | DECIMAL FOLLOWING
    ;

full_table_name
    : (linkedServer=id_ '.' '.' schema=id_   '.'
    |                       server=id_    '.' database=id_ '.'  schema=id_   '.'
    |                                         database=id_ '.'  schema=id_? '.'
    |                                                           schema=id_    '.')? table=id_
    ;

table_name
    : (database=id_ '.' schema=id_? '.' | schema=id_ '.')? (table=id_ | blocking_hierarchy=BLOCKING_HIERARCHY)
    ;

simple_name
    : (schema=id_ '.')? name=id_
    ;

func_proc_name_schema
    : ((schema=id_) '.')? procedure=id_
    ;

func_proc_name_database_schema
    : database=id_? '.' schema=id_? '.' procedure=id_
    | func_proc_name_schema
    ;

func_proc_name_server_database_schema
    : server=id_? '.' database=id_? '.' schema=id_? '.' procedure=id_
    | func_proc_name_database_schema
    ;

full_column_name
    : ((DELETED | INSERTED | full_table_name) '.')? (column_name=id_ | ('$' (IDENTITY | ROWGUID)))
    ;

column_name_list
    : col+=id_ (',' col+=id_)*
    ;

null_notnull
    : NOT? NULL_
    ;

scalar_function_name
    : func_proc_name_server_database_schema
    | RIGHT
    | LEFT
    | BINARY_CHECKSUM
    | CHECKSUM
    ;

// https://msdn.microsoft.com/en-us/library/ms187752.aspx
// TODO: implement runtime check or add new tokens.

data_type
    : scaled=(VARCHAR | NVARCHAR | BINARY_KEYWORD | VARBINARY_KEYWORD | SQUARE_BRACKET_ID) '(' MAX ')'
    | ext_type=id_ '(' scale=DECIMAL ',' prec=DECIMAL ')'
    | ext_type=id_ '(' scale=DECIMAL ')'
    | ext_type=id_ IDENTITY ('(' seed=DECIMAL ',' inc=DECIMAL ')')?
    | double_prec=DOUBLE PRECISION?
    | unscaled_type=id_
    ;

// https://msdn.microsoft.com/en-us/library/ms179899.aspx
constant
    : STRING // string, datetime or uniqueidentifier
    | BINARY
    | '-'? (DECIMAL | REAL | FLOAT)     // float or decimal
    | '-'? dollar='$' ('-'|'+')? (DECIMAL | FLOAT) // money
    | parameter
    ;

// To reduce ambiguity, -X is considered as an application of unary operator
primitive_constant
    : STRING // string, datetime or uniqueidentifier
    | BINARY
    | (DECIMAL | REAL | FLOAT)          // float or decimal
    | dollar='$' ('-'|'+')? (DECIMAL | FLOAT) // money
    | parameter
    ;

keyword
    : ABORT
    | ABSOLUTE
    | ACCENT_SENSITIVITY
    | ACCESS
    | ACTION
    | ACTIVATION
    | ACTIVE
    | ADD   // ?
    | ADDRESS
    | AES_128
    | AES_192
    | AES_256
    | AFFINITY
    | AFTER
    | AGGREGATE
    | ALGORITHM
    | ALL_CONSTRAINTS
    | ALL_ERRORMSGS
    | ALL_INDEXES
    | ALL_LEVELS
    | ALLOW_ENCRYPTED_VALUE_MODIFICATIONS
    | ALLOW_PAGE_LOCKS
    | ALLOW_ROW_LOCKS
    | ALLOW_SNAPSHOT_ISOLATION
    | ALLOWED
    | ALWAYS
    | ANSI_DEFAULTS
    | ANSI_NULL_DEFAULT
    | ANSI_NULL_DFLT_OFF
    | ANSI_NULL_DFLT_ON
    | ANSI_NULLS
    | ANSI_PADDING
    | ANSI_WARNINGS
    | APP_NAME
    | APPLICATION_LOG
    | APPLOCK_MODE
    | APPLOCK_TEST
    | APPLY
    | ARITHABORT
    | ARITHIGNORE
    | ASCII
    | ASSEMBLY
    | ASSEMBLYPROPERTY
    | AT_KEYWORD
    | AUDIT
    | AUDIT_GUID
    | AUTO
    | AUTO_CLEANUP
    | AUTO_CLOSE
    | AUTO_CREATE_STATISTICS
    | AUTO_DROP
    | AUTO_SHRINK
    | AUTO_UPDATE_STATISTICS
    | AUTO_UPDATE_STATISTICS_ASYNC
    | AUTOGROW_ALL_FILES
    | AUTOGROW_SINGLE_FILE
    | AVAILABILITY
    | AVG
    | BACKUP_CLONEDB
    | BACKUP_PRIORITY
    | BASE64
    | BEGIN_DIALOG
    | BIGINT
    | BINARY_KEYWORD
    | BINARY_CHECKSUM
    | BINDING
    | BLOB_STORAGE
    | BROKER
    | BROKER_INSTANCE
    | BULK_LOGGED
    | CALLER
    | CAP_CPU_PERCENT
    | CAST
    | TRY_CAST
    | CATALOG
    | CATCH
    | CERT_ID
    | CERTENCODED
    | CERTPRIVATEKEY
    | CHANGE
    | CHANGE_RETENTION
    | CHANGE_TRACKING
    | CHAR
    | CHARINDEX
    | CHECKALLOC
    | CHECKCATALOG
    | CHECKCONSTRAINTS
    | CHECKDB
    | CHECKFILEGROUP
    | CHECKSUM
    | CHECKSUM_AGG
    | CHECKTABLE
    | CLEANTABLE
    | CLEANUP
    | CLONEDATABASE
    | COL_LENGTH
    | COL_NAME
    | COLLECTION
    | COLUMN_ENCRYPTION_KEY
    | COLUMN_MASTER_KEY
    | COLUMNPROPERTY
    | COLUMNS
    | COLUMNSTORE
    | COLUMNSTORE_ARCHIVE
    | COMMITTED
    | COMPATIBILITY_LEVEL
    | COMPRESS_ALL_ROW_GROUPS
    | COMPRESSION_DELAY
    | CONCAT
    | CONCAT_WS
    | CONCAT_NULL_YIELDS_NULL
    | CONTENT
    | CONTROL
    | COOKIE
    | COUNT
    | COUNT_BIG
    | COUNTER
    | CPU
    | CREATE_NEW
    | CREATION_DISPOSITION
    | CREDENTIAL
    | CRYPTOGRAPHIC
    | CUME_DIST
    | CURSOR_CLOSE_ON_COMMIT
    | CURSOR_DEFAULT
    | CURSOR_STATUS
    | DATA
    | DATA_PURITY
    | DATABASE_PRINCIPAL_ID
    | DATABASEPROPERTYEX
    | DATALENGTH
    | DATE_CORRELATION_OPTIMIZATION
    | DATEADD
    | DATEDIFF
    | DATENAME
    | DATEPART
    | DAYS
    | DB_CHAINING
    | DB_FAILOVER
    | DB_ID
    | DB_NAME
    | DBCC
    | DBREINDEX
    | DECRYPTION
    | DEFAULT_DOUBLE_QUOTE
    | DEFAULT_FULLTEXT_LANGUAGE
    | DEFAULT_LANGUAGE
    | DEFINITION
    | DELAY
    | DELAYED_DURABILITY
    | DELETED
    | DENSE_RANK
    | DEPENDENTS
    | DES
    | DESCRIPTION
    | DESX
    | DETERMINISTIC
    | DHCP
    | DIALOG
    | DIFFERENCE
    | DIRECTORY_NAME
    | DISABLE
    | DISABLE_BROKER
    | DISABLED
    | DOCUMENT
    | DROP_EXISTING
    | DROPCLEANBUFFERS
    | DYNAMIC
    | ELEMENTS
    | EMERGENCY
    | EMPTY
    | ENABLE
    | ENABLE_BROKER
    | ENCRYPTED
    | ENCRYPTED_VALUE
    | ENCRYPTION
    | ENCRYPTION_TYPE
    | ENDPOINT_URL
    | ERROR_BROKER_CONVERSATIONS
    | ESTIMATEONLY
    | EXCLUSIVE
    | EXECUTABLE
    | EXIST
    | EXIST_SQUARE_BRACKET
    | EXPAND
    | EXPIRY_DATE
    | EXPLICIT
    | EXTENDED_LOGICAL_CHECKS
    | FAIL_OPERATION
    | FAILOVER_MODE
    | FAILURE
    | FAILURE_CONDITION_LEVEL
    | FAST
    | FAST_FORWARD
    | FILE_ID
    | FILE_IDEX
    | FILE_NAME
    | FILEGROUP
    | FILEGROUP_ID
    | FILEGROUP_NAME
    | FILEGROUPPROPERTY
    | FILEGROWTH
    | FILENAME
    | FILEPATH
    | FILEPROPERTY
    | FILEPROPERTYEX
    | FILESTREAM
    | FILTER
    | FIRST
    | FIRST_VALUE
    | FMTONLY
    | FOLLOWING
    | FORCE
    | FORCE_FAILOVER_ALLOW_DATA_LOSS
    | FORCED
    | FORCEPLAN
    | FORCESCAN
    | FORMAT
    | FORWARD_ONLY
    | FREE
    | FULLSCAN
    | FULLTEXT
    | FULLTEXTCATALOGPROPERTY
    | FULLTEXTSERVICEPROPERTY
    | GB
    | GENERATED
    | GETDATE
    | GETUTCDATE
    | GLOBAL
    | GO
    | GREATEST
    | GROUP_MAX_REQUESTS
    | GROUPING
    | GROUPING_ID
    | HADR
    | HAS_DBACCESS
    | HAS_PERMS_BY_NAME
    | HASH
    | HEALTH_CHECK_TIMEOUT
    | HIDDEN_KEYWORD
    | HIGH
    | HONOR_BROKER_PRIORITY
    | HOURS
    | IDENT_CURRENT
    | IDENT_INCR
    | IDENT_SEED
    | IDENTITY_VALUE
    | IGNORE_CONSTRAINTS
    | IGNORE_DUP_KEY
    | IGNORE_NONCLUSTERED_COLUMNSTORE_INDEX
    | IGNORE_REPLICATED_TABLE_CACHE
    | IGNORE_TRIGGERS
    | IMMEDIATE
    | IMPERSONATE
    | IMPLICIT_TRANSACTIONS
    | IMPORTANCE
    | INCLUDE_NULL_VALUES
    | INCREMENTAL
    | INDEX_COL
    | INDEXKEY_PROPERTY
    | INDEXPROPERTY
    | INITIATOR
    | INPUT
    | INSENSITIVE
    | INSERTED
    | INT
    | IP
    | IS_MEMBER
    | IS_ROLEMEMBER
    | IS_SRVROLEMEMBER
    | ISJSON
    | ISOLATION
    | JOB
    | JSON
    | JSON_OBJECT
    | JSON_ARRAY
    | JSON_VALUE
    | JSON_QUERY
    | JSON_MODIFY
    | JSON_PATH_EXISTS
    | KB
    | KEEP
    | KEEPDEFAULTS
    | KEEPFIXED
    | KEEPIDENTITY
    | KEY_SOURCE
    | KEYS
    | KEYSET
    | LAG
    | LAST
    | LAST_VALUE
    | LEAD
    | LEAST
    | LEN
    | LEVEL
    | LIST
    | LISTENER
    | LISTENER_URL
    | LOB_COMPACTION
    | LOCAL
    | LOCATION
    | LOCK
    | LOCK_ESCALATION
    | LOGIN
    | LOGINPROPERTY
    | LOOP
    | LOW
    | LOWER
    | LTRIM
    | MANUAL
    | MARK
    | MASKED
    | MATERIALIZED
    | MAX
    | MAX_CPU_PERCENT
    | MAX_DOP
    | MAX_FILES
    | MAX_IOPS_PER_VOLUME
    | MAX_MEMORY_PERCENT
    | MAX_PROCESSES
    | MAX_QUEUE_READERS
    | MAX_ROLLOVER_FILES
    | MAXDOP
    | MAXRECURSION
    | MAXSIZE
    | MB
    | MEDIUM
    | MEMORY_OPTIMIZED_DATA
    | MESSAGE
    | MIN
    | MIN_ACTIVE_ROWVERSION
    | MIN_CPU_PERCENT
    | MIN_IOPS_PER_VOLUME
    | MIN_MEMORY_PERCENT
    | MINUTES
    | MIRROR_ADDRESS
    | MIXED_PAGE_ALLOCATION
    | MODE
    | MODIFY
    | MODIFY_SQUARE_BRACKET
    | MOVE
    | MULTI_USER
    | NAME
    | NCHAR
    | NESTED_TRIGGERS
    | NEW_ACCOUNT
    | NEW_BROKER
    | NEW_PASSWORD
    | NEWNAME
    | NEXT
    | NO
    | NO_INFOMSGS
    | NO_QUERYSTORE
    | NO_STATISTICS
    | NO_TRUNCATE
    | NO_WAIT
    | NOCOUNT
    | NODES
    | NOEXEC
    | NOEXPAND
    | NOINDEX
    | NOLOCK
    | NON_TRANSACTED_ACCESS
    | NORECOMPUTE
    | NORECOVERY
    | NOTIFICATIONS
    | NOWAIT
    | NTILE
    | NULL_DOUBLE_QUOTE
    | NUMANODE
    | NUMBER
    | NUMERIC_ROUNDABORT
    | OBJECT
    | OBJECT_DEFINITION
    | OBJECT_ID
    | OBJECT_NAME
    | OBJECT_SCHEMA_NAME
    | OBJECTPROPERTY
    | OBJECTPROPERTYEX
    | OFFLINE
    | OFFSET
    | OLD_ACCOUNT
    | ONLINE
    | ONLY
    | OPEN_EXISTING
    | OPENJSON
    | OPTIMISTIC
    | OPTIMIZE
    | OPTIMIZE_FOR_SEQUENTIAL_KEY
    | ORIGINAL_DB_NAME
    | ORIGINAL_LOGIN
    | OUT
    | OUTPUT
    | OVERRIDE
    | OWNER
    | OWNERSHIP
    | PAD_INDEX
    | PAGE_VERIFY
    | PAGECOUNT
    | PAGLOCK
    | PARAMETERIZATION
    | PARSENAME
    | PARSEONLY
    | PARTITION
    | PARTITIONS
    | PARTNER
    | PATH
    | PATINDEX
    | PAUSE
    | PDW_SHOWSPACEUSED
    | PERCENT_RANK
    | PERCENTILE_CONT
    | PERCENTILE_DISC
    | PERMISSIONS
    | PERSIST_SAMPLE_PERCENT
    | PHYSICAL_ONLY
    | POISON_MESSAGE_HANDLING
    | POOL
    | PORT
    | PRECEDING
    | PRIMARY_ROLE
    | PRIOR
    | PRIORITY
    | PRIORITY_LEVEL
    | PRIVATE
    | PRIVATE_KEY
    | PRIVILEGES
    | PROCCACHE
    | PROCEDURE_NAME
    | PROPERTY
    | PROVIDER
    | PROVIDER_KEY_NAME
    | PWDCOMPARE
    | PWDENCRYPT
    | QUERY
    | QUERY_SQUARE_BRACKET
    | QUEUE
    | QUEUE_DELAY
    | QUOTED_IDENTIFIER
    | QUOTENAME
    | RANDOMIZED
    | RANGE
    | RANK
    | RC2
    | RC4
    | RC4_128
    | READ_COMMITTED_SNAPSHOT
    | READ_ONLY
    | READ_ONLY_ROUTING_LIST
    | READ_WRITE
    | READCOMMITTED
    | READCOMMITTEDLOCK
    | READONLY
    | READPAST
    | READUNCOMMITTED
    | READWRITE
    | REBUILD
    | RECEIVE
    | RECOMPILE
    | RECOVERY
    | RECURSIVE_TRIGGERS
    | RELATIVE
    | REMOTE
    | REMOTE_PROC_TRANSACTIONS
    | REMOTE_SERVICE_NAME
    | REMOVE
    | REORGANIZE
    | REPAIR_ALLOW_DATA_LOSS
    | REPAIR_FAST
    | REPAIR_REBUILD
    | REPEATABLE
    | REPEATABLEREAD
    | REPLACE
    | REPLICA
    | REPLICATE
    | REQUEST_MAX_CPU_TIME_SEC
    | REQUEST_MAX_MEMORY_GRANT_PERCENT
    | REQUEST_MEMORY_GRANT_TIMEOUT_SEC
    | REQUIRED_SYNCHRONIZED_SECONDARIES_TO_COMMIT
    | RESAMPLE
    | RESERVE_DISK_SPACE
    | RESOURCE
    | RESOURCE_MANAGER_LOCATION
    | RESTRICTED_USER
    | RESUMABLE
    | RETENTION
    | REVERSE
    | ROBUST
    | ROOT
    | ROUTE
    | ROW
    | ROW_NUMBER
    | ROWGUID
    | ROWLOCK
    | ROWS
    | RTRIM
    | SAMPLE
    | SCHEMA_ID
    | SCHEMA_NAME
    | SCHEMABINDING
    | SCOPE_IDENTITY
    | SCOPED
    | SCROLL
    | SCROLL_LOCKS
    | SEARCH
    | SECONDARY
    | SECONDARY_ONLY
    | SECONDARY_ROLE
    | SECONDS
    | SECRET
    | SECURABLES
    | SECURITY
    | SECURITY_LOG
    | SEEDING_MODE
    | SELF
    | SEMI_SENSITIVE
    | SEND
    | SENT
    | SEQUENCE
    | SEQUENCE_NUMBER
    | SERIALIZABLE
    | SERVERPROPERTY
    | SERVICEBROKER
    | SESSIONPROPERTY
    | SESSION_TIMEOUT
    | SETERROR
    | SHARE
    | SHARED
    | SHOWCONTIG
    | SHOWPLAN
    | SHOWPLAN_ALL
    | SHOWPLAN_TEXT
    | SHOWPLAN_XML
    | SIGNATURE
    | SIMPLE
    | SINGLE_USER
    | SIZE
    | SMALLINT
    | SNAPSHOT
    | SORT_IN_TEMPDB
    | SOUNDEX
    | SPACE_KEYWORD
    | SPARSE
    | SPATIAL_WINDOW_MAX_CELLS
    | SQL_VARIANT_PROPERTY
    | STANDBY
    | START_DATE
    | STATIC
    | STATISTICS_INCREMENTAL
    | STATISTICS_NORECOMPUTE
    | STATS_DATE
    | STATS_STREAM
    | STATUS
    | STATUSONLY
    | STDEV
    | STDEVP
    | STOPLIST
    | STR
    | STRING_AGG
    | STRING_ESCAPE
    | STUFF
    | SUBJECT
    | SUBSCRIBE
    | SUBSCRIPTION
    | SUBSTRING
    | SUM
    | SUSER_ID
    | SUSER_NAME
    | SUSER_SID
    | SUSER_SNAME
    | SUSPEND
    | SYMMETRIC
    | SYNCHRONOUS_COMMIT
    | SYNONYM
    | SYSTEM
    | TABLERESULTS
    | TABLOCK
    | TABLOCKX
    | TAKE
    | TARGET_RECOVERY_TIME
    | TB
    | TEXTIMAGE_ON
    | THROW
    | TIES
    | TIME
    | TIMEOUT
    | TIMER
    | TINYINT
    | TORN_PAGE_DETECTION
    | TRACKING
    | TRANSACTION_ID
    | TRANSFORM_NOISE_WORDS
    | TRANSLATE
    | TRIM
    | TRIPLE_DES
    | TRIPLE_DES_3KEY
    | TRUSTWORTHY
    | TRY
    | TSQL
    | TWO_DIGIT_YEAR_CUTOFF
    | TYPE
    | TYPE_ID
    | TYPE_NAME
    | TYPE_WARNING
    | TYPEPROPERTY
    | UNBOUNDED
    | UNCOMMITTED
    | UNICODE
    | UNKNOWN
    | UNLIMITED
    | UNMASK
    | UOW
    | UPDLOCK
    | UPPER
    | USER_ID
    | USER_NAME
    | USING
    | VALID_XML
    | VALIDATION
    | VALUE
    | VALUE_SQUARE_BRACKET
    | VAR
    | VARBINARY_KEYWORD
    | VARP
    | VERIFY_CLONEDB
    | VERSION
    | VIEW_METADATA
    | VIEWS
    | WAIT
    | WELL_FORMED_XML
    | WITHOUT_ARRAY_WRAPPER
    | WORK
    | WORKLOAD
    | XLOCK
    | XML
    | XML_COMPRESSION
    | XMLDATA
    | XMLNAMESPACES
    | XMLSCHEMA
    | XSINIL
    | ZONE
//More keywords that can also be used as IDs
    | ABORT_AFTER_WAIT
    | ABSENT
    | ADMINISTER
    | AES
    | ALLOW_CONNECTIONS
    | ALLOW_MULTIPLE_EVENT_LOSS
    | ALLOW_SINGLE_EVENT_LOSS
    | ANONYMOUS
    | APPEND
    | APPLICATION
    | ASYMMETRIC
    | ASYNCHRONOUS_COMMIT
    | AUTHENTICATE
    | AUTHENTICATION
    | AUTOMATED_BACKUP_PREFERENCE
    | AUTOMATIC
    | AVAILABILITY_MODE
    | BEFORE
    | BLOCK
    | BLOCKERS
    | BLOCKSIZE
    | BLOCKING_HIERARCHY
    | BUFFER
    | BUFFERCOUNT
    | CACHE
    | CALLED
    | CERTIFICATE
    | CHANGETABLE
    | CHANGES
    | CHECK_POLICY
    | CHECK_EXPIRATION
    | CLASSIFIER_FUNCTION
    | CLUSTER
    | COMPRESS
    | COMPRESSION
    | CONNECT
    | CONNECTION
    | CONFIGURATION
    | CONNECTIONPROPERTY
    | CONTAINMENT
    | CONTEXT
    | CONTEXT_INFO
    | CONTINUE_AFTER_ERROR
    | CONTRACT
    | CONTRACT_NAME
    | CONVERSATION
    | COPY_ONLY
    | CURRENT_REQUEST_ID
    | CURRENT_TRANSACTION_ID
    | CYCLE
    | DATA_COMPRESSION
    | DATA_SOURCE
    | DATABASE_MIRRORING
    | DATASPACE
    | DDL
    | DECOMPRESS
    | DEFAULT_DATABASE
    | DEFAULT_SCHEMA
    | DIAGNOSTICS
    | DIFFERENTIAL
    | DISTRIBUTION
    | DTC_SUPPORT
    | ENABLED
    | ENDPOINT
    | ERROR
    | ERROR_LINE
    | ERROR_MESSAGE
    | ERROR_NUMBER
    | ERROR_PROCEDURE
    | ERROR_SEVERITY
    | ERROR_STATE
    | EVENT
    | EVENTDATA
    | EVENT_RETENTION_MODE
    | EXECUTABLE_FILE
    | EXPIREDATE
    | EXTENSION
    | EXTERNAL_ACCESS
    | FAILOVER
    | FAILURECONDITIONLEVEL
    | FAN_IN
    | FILE_SNAPSHOT
    | FORCESEEK
    | FORCE_SERVICE_ALLOW_DATA_LOSS
    | FORMATMESSAGE
    | GET
    | GET_FILESTREAM_TRANSACTION_CONTEXT
    | GETANCESTOR
    | GETANSINULL
    | GETDESCENDANT
    | GETLEVEL
    | GETREPARENTEDVALUE
    | GETROOT
    | GOVERNOR
    | HASHED
    | HEALTHCHECKTIMEOUT
    | HEAP
    | HIERARCHYID
    | HOST_ID
    | HOST_NAME
    | IIF
    | IO
    | INCLUDE
    | INCREMENT
    | INFINITE
    | INIT
    | INSTEAD
    | ISDESCENDANTOF
    | ISNULL
    | ISNUMERIC
    | KERBEROS
    | KEY_PATH
    | KEY_STORE_PROVIDER_NAME
    | LANGUAGE
    | LIBRARY
    | LIFETIME
    | LINKED
    | LINUX
    | LISTENER_IP
    | LISTENER_PORT
    | LOCAL_SERVICE_NAME
    | LOG
    | MASK
    | MATCHED
    | MASTER
    | MAX_MEMORY
    | MAXTRANSFER
    | MAXVALUE
    | MAX_DISPATCH_LATENCY
    | MAX_DURATION
    | MAX_EVENT_SIZE
    | MAX_SIZE
    | MAX_OUTSTANDING_IO_PER_VOLUME
    | MEDIADESCRIPTION
    | MEDIANAME
    | MEMBER
    | MEMORY_PARTITION_MODE
    | MESSAGE_FORWARDING
    | MESSAGE_FORWARD_SIZE
    | MINVALUE
    | MIRROR
    | MUST_CHANGE
    | NEWID
    | NEWSEQUENTIALID
    | NOFORMAT
    | NOINIT
    | NONE
    | NOREWIND
    | NOSKIP
    | NOUNLOAD
    | NO_CHECKSUM
    | NO_COMPRESSION
    | NO_EVENT_LOSS
    | NOTIFICATION
    | NTLM
    | OLD_PASSWORD
    | ON_FAILURE
    | OPERATIONS
    | PAGE
    | PARAM_NODE
    | PARTIAL
    | PASSWORD
    | PERMISSION_SET
    | PER_CPU
    | PER_DB
    | PER_NODE
    | PERSISTED
    | PLATFORM
    | POLICY
    | PREDICATE
    | PROCESS
    | PROFILE
    | PYTHON
    | R
    | READ_WRITE_FILEGROUPS
    | REGENERATE
    | RELATED_CONVERSATION
    | RELATED_CONVERSATION_GROUP
    | REQUIRED
    | RESET
    | RESOURCES
    | RESTART
    | RESUME
    | RETAINDAYS
    | RETURNS
    | REWIND
    | ROLE
    | ROUND_ROBIN
    | ROWCOUNT_BIG
    | RSA_512
    | RSA_1024
    | RSA_2048
    | RSA_3072
    | RSA_4096
    | SAFETY
    | SAFE
    | SCHEDULER
    | SCHEME
    | SCRIPT
    | SERVER
    | SERVICE
    | SERVICE_BROKER
    | SERVICE_NAME
    | SESSION
    | SESSION_CONTEXT
    | SETTINGS
    | SHRINKLOG
    | SID
    | SKIP_KEYWORD
    | SOFTNUMA
    | SOURCE
    | SPECIFICATION
    | SPLIT
    | SQL
    | SQLDUMPERFLAGS
    | SQLDUMPERPATH
    | SQLDUMPERTIMEOUT
    | STATE
    | STATS
    | START
    | STARTED
    | STARTUP_STATE
    | STOP
    | STOPPED
    | STOP_ON_ERROR
    | SUPPORTED
    | SWITCH
    | TAPE
    | TARGET
    | TCP
    | TOSTRING
    | TRACE
    | TRACK_CAUSALITY
    | TRANSFER
    | UNCHECKED
    | UNLOCK
    | UNSAFE
    | URL
    | USED
    | VERBOSELOGGING
    | VISIBILITY
    | WAIT_AT_LOW_PRIORITY
    | WINDOWS
    | WITHOUT
    | WITNESS
    | XACT_ABORT
    | XACT_STATE
    //
    | ABS
    | ACOS
    | ASIN
    | ATAN
    | ATN2
    | CEILING
    | COS
    | COT
    | DEGREES
    | EXP
    | FLOOR
    | LOG10
    | PI
    | POWER
    | RADIANS
    | RAND
    | ROUND
    | SIGN
    | SIN
    | SQRT
    | SQUARE
    | TAN
    //
    | CURRENT_TIMEZONE
    | CURRENT_TIMEZONE_ID
    | DATE_BUCKET
    | DATEDIFF_BIG
    | DATEFROMPARTS
    | DATETIME2FROMPARTS
    | DATETIMEFROMPARTS
    | DATETIMEOFFSETFROMPARTS
    | DATETRUNC
    | DAY
    | EOMONTH
    | ISDATE
    | MONTH
    | SMALLDATETIMEFROMPARTS
    | SWITCHOFFSET
    | SYSDATETIME
    | SYSDATETIMEOFFSET
    | SYSUTCDATETIME
    | TIMEFROMPARTS
    | TODATETIMEOFFSET
    | YEAR
    //
    | QUARTER
    | DAYOFYEAR
    | WEEK
    | HOUR
    | MINUTE
    | SECOND
    | MILLISECOND
    | MICROSECOND
    | NANOSECOND
    | TZOFFSET
    | ISO_WEEK
    | WEEKDAY
    //
    | YEAR_ABBR
    | QUARTER_ABBR
    | MONTH_ABBR
    | DAYOFYEAR_ABBR
    | DAY_ABBR
    | WEEK_ABBR
    | HOUR_ABBR
    | MINUTE_ABBR
    | SECOND_ABBR
    | MILLISECOND_ABBR
    | MICROSECOND_ABBR
    | NANOSECOND_ABBR
    | TZOFFSET_ABBR
    | ISO_WEEK_ABBR
    | WEEKDAY_ABBR
    //
    | SP_EXECUTESQL
    //Build-ins:
    | VARCHAR
    | NVARCHAR
    | PRECISION //For some reason this is possible to use as ID
    ;

// https://msdn.microsoft.com/en-us/library/ms175874.aspx
id_
    : ID
    | TEMP_ID
    | DOUBLE_QUOTE_ID
    | DOUBLE_QUOTE_BLANK
    | SQUARE_BRACKET_ID
    | keyword
    ;

// https://msdn.microsoft.com/en-us/library/ms188074.aspx
// Spaces are allowed for comparison operators.
comparison_operator
    : '=' | '>' | '<' | '<' '=' | '>' '=' | '<' '>' | '!' '=' | '!' '>' | '!' '<'
    ;

assignment_operator
    : '+=' | '-=' | '*=' | '/=' | '%=' | '&=' | '^=' | '|='
    ;
