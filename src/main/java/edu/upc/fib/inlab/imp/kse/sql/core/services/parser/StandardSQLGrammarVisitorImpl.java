package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.services.builders.TableSetBuilder;

import java.util.ArrayList;
import java.util.List;

public class StandardSQLGrammarVisitorImpl extends SQLParserBaseVisitor {

    private final SQLObjectSchema schema;
    private final List<Query> queries;

    /**
     * When a parsed table is referenced without any SchemaReference, this default schema reference is used.
     */
    private final SchemaReference defaultSchemaReference;

    /**
     * Needed for creating table objects
     */
    private TableSetBuilder builder;
    private boolean tablesInStandBy;

    /**
     * Needed for unnamed constraints.
     */
    private final String unnamedConstraintName = "constraint";
    private int unnamedConstraintNumber = 1;

    public StandardSQLGrammarVisitorImpl(SQLObjectSchema schema, List<Query> queries, SchemaReference defaultSchemaReference) {
        this.schema = schema;
        this.defaultSchemaReference = defaultSchemaReference;
        this.builder = new TableSetBuilder();
        this.tablesInStandBy = false;
        this.queries = queries;
    }

    private void generateTableBatch() {
        schema.addTables(builder.build());
        this.builder = new TableSetBuilder();
        this.tablesInStandBy = false;
    }

    /* TOP LEVEL STATEMENTS NODES */

    public Object visitSql_file(SQLParser.Sql_fileContext ctx) {
        Object o = visitChildren(ctx);
        generateTableBatch(); // If input ends with CREATE TABLE statements, they need to be created at the EOF.
        return o;
    }

    public Object visitDml_clause(SQLParser.Dml_clauseContext ctx) {
        if (ctx.select_statement_standalone() == null) throw new IMPSqlException("Unknown DML clause.");
        Query query = visitSelect_statement_standalone(ctx.select_statement_standalone());
        queries.add(query);
        return query;
    }

    public Assertion visitCreate_assertion(SQLParser.Create_assertionContext ctx) {
        if (tablesInStandBy) generateTableBatch();

        Assertion newAssertion = new Assertion(
            visitId_(ctx.table_name().table),
            visitTable_name(ctx.table_name()),
            visitAssertion_check(ctx.assertion_check()));
        schema.addAssertion(newAssertion);
        return newAssertion;
    }

    public BooleanExpression visitAssertion_check(SQLParser.Assertion_checkContext ctx) {
        return visitSearch_condition(ctx.search_condition());
    }


    public Query visitSelect_statement_standalone(SQLParser.Select_statement_standaloneContext ctx)  {
        if (ctx.with_expression() != null) throw new RuntimeException("Grammar expression (`WITH`) not supported yet!");
        return visitSelect_statement(ctx.select_statement());
    }

    public Query visitSelect_statement(SQLParser.Select_statementContext ctx) {
        if (ctx.select_order_by_clause() != null) throw new RuntimeException("Grammar expression (`ORDER BY`) not supported yet!");
        if (ctx.for_clause() != null) throw new RuntimeException("Grammar expression (`FOR ...`) not supported yet!");
        if (ctx.option_clause() != null) throw new RuntimeException("Grammar expression (`OPTION ...`) not supported yet!");
        return visitQuery_expression(ctx.query_expression());
    }

    public View visitCreate_view(SQLParser.Create_viewContext ctx) {
        if (ctx.WITH().size() > 0) throw new RuntimeException("Grammar expression (`WITH ...`) in create view not supported yet!");

        if (tablesInStandBy) generateTableBatch();

        List<String> columnNames = null;
        if (ctx.column_name_list() != null) columnNames = visitColumn_name_list(ctx.column_name_list());
        View view = new View(
            visitId_(ctx.simple_name().name),
            visitSimple_name(ctx.simple_name()),
            columnNames,
            visitSelect_statement_standalone(ctx.select_statement_standalone()));
        schema.addView(view);
        return view;
    }

    public List<String> visitColumn_name_list(SQLParser.Column_name_listContext ctx) {
        return ctx.col.stream().map(this::visitId_).toList();
    }

    public Table visitCreate_table(SQLParser.Create_tableContext ctx) {
        if (ctx.table_name().BLOCKING_HIERARCHY() != null) throw new RuntimeException("Grammar expression (`BLOCKING_HIERARCHY`) not supported yet!");

        if (!ctx.table_indices().isEmpty()) throw new RuntimeException("Table indices not supported yet!");
        if (ctx.LOCK() != null) throw new RuntimeException("Grammar expression (`LOCK`) not supported yet!");
        if (!ctx.table_options().isEmpty()) throw new RuntimeException("Table options not supported yet!");
        if (ctx.ON() != null || !ctx.DEFAULT().isEmpty()) throw new RuntimeException("Table options not supported yet!");
        if (ctx.TEXTIMAGE_ON() != null || !ctx.DEFAULT().isEmpty()) throw new RuntimeException("Grammar expression (`TEXTIMAGE_ON`) not supported yet!");

        SchemaReference schemaReference = visitTable_name(ctx.table_name());
        String tableName = visitId_(ctx.table_name().table);
        this.builder.addTable(schemaReference, tableName);
        this.tablesInStandBy = true;

        for (SQLParser.Column_def_table_constraintContext c : ctx.column_def_table_constraints().column_def_table_constraint()) {
            visitColumn_def_table_constraint(c, schemaReference, tableName);
        }
        return null;
    }

    /** BOOLEAN EXPRESSION / ASSERTION NODES **/


    public BooleanExpression visitSearch_condition(SQLParser.Search_conditionContext ctx) {
        if (ctx.AND() != null)  {
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        }
        else if (ctx.OR() != null)  {
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.OR,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        }
        else {
            BooleanExpression expression;
            if (ctx.predicate() != null) expression = visitPredicate(ctx.predicate());
            else expression = visitSearch_condition(ctx.search_condition(0));

            for (int i = 0; i < ctx.NOT().size(); i++) {
                expression = new NotOperation(expression);
            }

            return expression;
        }
    }

    public BooleanExpression visitPredicate(SQLParser.PredicateContext ctx) {
        if (ctx.EXISTS() != null) return new ExistsPredicate(visitSubquery(ctx.subquery()));
        else if (ctx.comparison_operator() != null && ctx.subquery() == null) {
            return new ComparisonPredicate(
                visitComparison_operator(ctx.comparison_operator()),
                visitExpression(ctx.expression(0)),
                visitExpression(ctx.expression(1)));
        }
        else if (ctx.IN() != null) {
            BooleanExpression expression = new ValueListInPredicate(
                visitExpression(ctx.expression(0)),
                visitExpression_list(ctx.expression_list())
            );
            for (int i = 0; i < ctx.NOT().size(); i++) expression = new NotOperation(expression);
            return expression;
        }
        else {
            throw new RuntimeException("Grammar expression of different predicates not supported yet!");
        }
    }

    /** VALUE EXPRESSION NODES **/

    public ValueExpression visitExpression(SQLParser.ExpressionContext ctx) {
        if (ctx.primitive_expression() != null) return visitPrimitive_expression(ctx.primitive_expression());
        if(ctx.full_column_name() != null) return visitFull_column_name(ctx.full_column_name());
        if(ctx.bracket_expression() != null) return visitBracket_expression(ctx.bracket_expression());
        if (ctx.function_call() != null) return visitFunction_call(ctx.function_call());
        throw new RuntimeException("Grammar expression of other expressions not supported yet!");
    }

    public ValueExpression visitBracket_expression(SQLParser.Bracket_expressionContext ctx) {
        if (ctx.expression() != null) return visitExpression(ctx.expression());
        else return visitSubquery(ctx.subquery());
    }

    public ValueExpression visitFunction_call(SQLParser.Function_callContext ctx) {
        if (ctx.scalar_function_name() == null) {
            throw new RuntimeException("Grammar expression (`"+ctx.getText() +"`) not supported yet!");
        }

        SQLParser.Expression_listContext expression_listContext = ctx.expression_list();
        if (expression_listContext == null) {
            return new SQLFunction(ctx.scalar_function_name().getText());
        } else {
            List<ValueExpression> arguments = visitExpression_list(expression_listContext);
            return new SQLFunction(ctx.scalar_function_name().getText(), arguments);
        }
    }

    public List<ValueExpression> visitExpression_list(SQLParser.Expression_listContext ctx) {
        return ctx.expression().stream().map(this::visitExpression).toList();
    }

    public ColumnReference visitFull_column_name(SQLParser.Full_column_nameContext ctx) {
        String tableName = null;
        if (ctx.DELETED() != null || ctx.INSERTED() != null || ctx.IDENTITY() != null || ctx.ROWGUID() != null) {
            throw new RuntimeException("Grammar expression related to extra info for full_column_name not supported yet!");
        }
        else if (ctx.full_table_name() != null) {
            tableName = visitId_(ctx.full_table_name().table);
            SchemaReference schemaReference = visitFull_table_name(ctx.full_table_name());
            if (schemaReference != visitFull_table_name(ctx.full_table_name())) {
                throw new RuntimeException("schema reference definition in a column reference not supported yet");
            }
        }

        return new ColumnReference(tableName, visitId_(ctx.column_name));
    }

    /** QUERY NODES **/

    public Query visitQuery_expression(SQLParser.Query_expressionContext ctx) {
        if (ctx.select_order_by_clause() != null) throw new RuntimeException("Grammar expression (`ORDER BY`) not supported yet!");
        if (ctx.UNION() != null) {
            //TODO: Future Work - IMPSQL-46, IMPSQL-50
            throw new RuntimeException("UNIONS not supported yet!");
        }
        if (ctx.unions.size() != 0) {
            //TODO: Future Work - IMPSQL-46, IMPSQL-50
            throw new RuntimeException("UNIONS not supported yet!");
        }
        if (!ctx.query_expression().isEmpty()) return visitQuery_expression(ctx.query_expression(0));
        else return visitQuerySpecification(ctx.query_specification());
    }

    public TableExpression visitQuerySpecification(SQLParser.Query_specificationContext ctx) {
        if (ctx.allOrDistinct != null) throw new RuntimeException("Grammar expression (`ALL/DISTINCT`) not supported yet!");
        if(ctx.top != null) throw new RuntimeException("Grammar expression (`TOP`) not supported yet!");
        if(ctx.INTO() != null) throw new RuntimeException("Grammar expression (`TOP`) not supported yet!");
        if(ctx.GROUP() != null) throw new RuntimeException("Grammar expression (`GROUP BY`) not supported yet!");
        if(ctx.HAVING() != null) throw new RuntimeException("Grammar expression (`HAVING`) not supported yet!");

        RelationalExpression fromClause = null;
        BooleanExpression whereClause = null;

        List<SelectItem> selectClause = visitSelect_list(ctx.columns);
        if (ctx.FROM() != null) fromClause = visitTable_sources(ctx.from);
        if (ctx.WHERE() != null) whereClause = visitSearch_condition(ctx.where);

        return new TableExpression(selectClause, fromClause, whereClause, null);
    }

    public List<SelectItem> visitSelect_list(SQLParser.Select_listContext ctx) {
        List<SelectItem> columns = new ArrayList<>();
        for (SQLParser.Select_list_elemContext item : ctx.selectElement) {
            columns.add(visitSelect_list_elem(item));
        }
        return columns;
    }

    public SelectItem visitSelect_list_elem(SQLParser.Select_list_elemContext ctx) {
        if (ctx.udt_elem() != null) throw new RuntimeException("Grammar expression (udt elements) not supported yet!");
        if (ctx.LOCAL_ID() != null) throw new RuntimeException("Grammar expression (Local id variables) not supported yet!");

        if (ctx.asterisk() != null) return visitAsterisk(ctx.asterisk());
        else return visitExpression_elem(ctx.expression_elem());

    }

    public Asterisk visitAsterisk(SQLParser.AsteriskContext ctx) {
        if (ctx.table_name() != null
            || ctx.INSERTED() != null
            || ctx.DELETED() != null) throw new RuntimeException("Grammar expression (`TABLE.*`) not supported yet!");
        return new Asterisk();
    }

    public AliasableSelectItem visitExpression_elem(SQLParser.Expression_elemContext ctx) {
        if (ctx.eq != null) return new AliasableSelectItem(visitExpression(ctx.leftAssignment), ctx.leftAlias.getText());
        else {
            String alias = null;
            if (ctx.as_column_alias() != null) alias = visitAs_column_alias(ctx.as_column_alias());
            return new AliasableSelectItem(visitExpression(ctx.expressionAs), alias);
        }
    }

    public String visitAs_table_alias(SQLParser.As_table_aliasContext ctx) {
        return ctx.table_alias().getText();
    }

    public String visitAs_column_alias(SQLParser.As_column_aliasContext ctx) {
        return visitColumn_alias(ctx.column_alias());
    }

    public String visitColumn_alias(SQLParser.Column_aliasContext ctx) {
        if (ctx.STRING() != null) throw new RuntimeException("Grammar expression (STRING) in alias expressions not supported yet!");
        return visitId_(ctx.id_());
    }


    public RelationalExpression visitTable_sources(SQLParser.Table_sourcesContext ctx) {
        if (ctx.non_ansi_join() != null) return visitNon_ansi_join(ctx.non_ansi_join());

        RelationalExpression root = visitTable_source(ctx.source.get(0));
        for (int i = 1; i < ctx.source.size(); i++) {
            root = new CrossJoin(root, visitTable_source(ctx.source.get(i)));
        }
        return root;
    }

    public RelationalExpression visitNon_ansi_join(SQLParser.Non_ansi_joinContext ctx) {
        RelationalExpression root = visitTable_source(ctx.source.get(0));
        for (int i = 1; i < ctx.source.size(); i++) {
            root = new CrossJoin(root, visitTable_source(ctx.source.get(i)));
        }
        return root;
    }

    public RelationalExpression visitTable_source(SQLParser.Table_sourceContext ctx) {
        RelationalExpression root = visitTable_source_item(ctx.table_source_item());
        for (SQLParser.Join_partContext join_part : ctx.join_part()) {
            RelationalExpression rightExpression = visitJoin_part(join_part);
            if (join_part.join_on() != null) {
                BooleanExpression onCondition = visitSearch_condition(join_part.join_on().search_condition());
                if (join_part.join_on().inner != null)
                    root = new OnJoin(OnJoin.JoinOperator.INNER, root, rightExpression, onCondition);
                else if (join_part.join_on().LEFT() != null)
                    root = new OnJoin(OnJoin.JoinOperator.LEFT, root, rightExpression, onCondition);
                else if (join_part.join_on().RIGHT() != null)
                    root = new OnJoin(OnJoin.JoinOperator.RIGHT, root, rightExpression, onCondition);
                else if (join_part.join_on().FULL() != null)
                    root = new OnJoin(OnJoin.JoinOperator.FULL, root, rightExpression, onCondition);
                else //if non specified inner join is considered
                    root = new OnJoin(OnJoin.JoinOperator.INNER, root, rightExpression, onCondition);
            }
            else if (join_part.cross_join() != null) {
                root = new CrossJoin(root, rightExpression);
            }
        }
        return root;
    }

    public RelationalExpression visitJoin_part(SQLParser.Join_partContext ctx) {
        if (ctx.apply_() != null) throw new RuntimeException("apply_ joins not supported yet!");
        if (ctx.pivot() != null || ctx.unpivot() != null) throw new RuntimeException("pivot joins not supported yet!");

        if (ctx.join_on() != null) return visitJoin_on(ctx.join_on());
        if (ctx.cross_join() != null) return visitCross_join(ctx.cross_join());

        throw new RuntimeException("Unknown join operation");
    }

    /**
     * On condition not processed in upper visitors.
     */
    public RelationalExpression visitJoin_on(SQLParser.Join_onContext ctx) {
        if (ctx.join_hint != null) throw new RuntimeException("join_hint not supported yet!");
        return visitTable_source(ctx.source);
    }

    public RelationalExpression visitCross_join(SQLParser.Cross_joinContext ctx) {
        return visitTable_source_item(ctx.table_source_item());
    }

    public RelationalExpression visitTable_source_item(SQLParser.Table_source_itemContext ctx) {
        if (ctx.column_alias_list() != null) throw new RuntimeException("column_alias_list not supported yet!");

        String alias = null;
        if (ctx.as_table_alias() != null) alias = visitAs_table_alias(ctx.as_table_alias());

        if (ctx.full_table_name() != null) {
            if (ctx.deprecated_table_hint() != null
                || ctx.with_table_hints() != null
                || ctx.sybase_legacy_hints() != null) throw new RuntimeException("Grammar expression related to table_source_item not supported yet!");

            String tableName = visitId_(ctx.full_table_name().table);

            SchemaReference schemaReference = visitFull_table_name(ctx.full_table_name());
            return new TableReference(schema.getTable(tableName, schemaReference), alias);
        }
        if (ctx.derived_table() != null) return visitDerived_table(ctx.derived_table()).getAliasedCopy(alias);
        if (ctx.table_source() != null) return visitTable_source(ctx.table_source());

        throw new RuntimeException("Other table_source_item types not supported yet!");
    }

    public Query visitDerived_table(SQLParser.Derived_tableContext ctx) {
        if (ctx.table_value_constructor() != null) throw new RuntimeException("Grammar expression table_value_constructor not supported yet!");

        Query root = visitSubquery(ctx.subquery().get(0));
        //TODO: Future Work - IMPSQL-46, IMPSQL-50
        if (ctx.subquery().size() > 1) throw new RuntimeException("UNIONS not supported yet!");
        return root;
    }

    public Query visitSubquery(SQLParser.SubqueryContext ctx) {
        return visitSelect_statement(ctx.select_statement());
    }

    /** TABLE NODES **/

    public SchemaReference visitTable_name(SQLParser.Table_nameContext ctx) {
        if (ctx.BLOCKING_HIERARCHY() != null) throw new RuntimeException("Grammar expression (`BLOCKING_HIERARCHY`) not supported yet!");
        if (ctx.schema == null) return defaultSchemaReference;
        else if (ctx.database == null) return new SchemaReference(ctx.schema.getText());
        else return new SchemaReference(ctx.database.getText(), ctx.schema.getText());
    }

    public void visitColumn_def_table_constraint(SQLParser.Column_def_table_constraintContext ctx, SchemaReference schemaReference, String tableName) {
        if (ctx.materialized_column_definition() != null) throw new RuntimeException("Materialized column definition not supported yet!");
        if (ctx.column_definition() != null) visitColumn_definition(ctx.column_definition(), schemaReference, tableName);
        else if (ctx.table_constraint() != null) visitTable_constraint(ctx.table_constraint(), schemaReference, tableName);
    }

    private void visitColumn_definition(SQLParser.Column_definitionContext ctx, SchemaReference schemaReference, String tableName) {
        if (ctx.column_index() != null) throw new RuntimeException("Column indexes not supported yet!");
        if (ctx.AS() != null) throw new RuntimeException("Columns defined as expressions not supported yet!");

        String attributeName = visitId_(ctx.id_());
        builder.addAttribute(schemaReference, tableName, attributeName, visitData_type(ctx.data_type()));

        for (SQLParser.Column_definition_elementContext defCtx : ctx.column_definition_element()) {
            visitColumn_Definition_Element(defCtx, schemaReference, tableName, attributeName);
        }
    }

    public SQLDataType visitData_type(SQLParser.Data_typeContext ctx) {
        if (ctx.CHAR() != null || ctx.CHARACTER() != null) {
            if (ctx.VARYING() == null) return ctx.length != null ? new SQLCharacter(Integer.parseInt(ctx.length.getText())) : new SQLCharacter();
            else return new SQLVarchar(Integer.parseInt(ctx.length.getText()));
        }
        else if (ctx.VARCHAR() != null) return new SQLVarchar(Integer.parseInt(ctx.length.getText()));
        else if (ctx.NATIONAL() != null) throw new RuntimeException("NATIONAL characters not supported yet!");

        else if (ctx.BIT() != null) {
            if (ctx.VARYING() == null) return ctx.length != null ? new SQLBit(Integer.parseInt(ctx.length.getText())) : new SQLBit();
            else return new SQLVarbit(Integer.parseInt(ctx.length.getText()));
        }

        else if (ctx.NUMERIC() != null) {
            if (ctx.prec != null) {
                if (ctx.scale != null) {
                    return new SQLNumeric(Integer.parseInt(ctx.prec.getText()), Integer.parseInt(ctx.scale.getText()));
                }
                else return new SQLNumeric(Integer.parseInt(ctx.prec.getText()));
            }
            else return new SQLNumeric();
        }
        if (ctx.DECIMAL_() != null || ctx.DEC_() != null) {
            if (ctx.prec != null) {
                if (ctx.scale != null) {
                    return new SQLDecimal(Integer.parseInt(ctx.prec.getText()), Integer.parseInt(ctx.scale.getText()));
                }
                else return new SQLDecimal(Integer.parseInt(ctx.prec.getText()));
            }
            else return new SQLDecimal();
        }
        else if (ctx.INT() != null || ctx.INTEGER() != null) return new SQLInteger();
        else if (ctx.SMALLINT() != null) return new SQLSmallint();
        else if (ctx.FLOAT_() != null) return ctx.prec != null ? new SQLFloat(Integer.parseInt(ctx.prec.getText())) : new SQLFloat();
        else if (ctx.REAL_() != null) return new SQLReal();
        else if (ctx.DOUBLE() != null) return new SQLDoublePrecision();

        else if (ctx.ZONE() != null) throw new RuntimeException("'WITH TIME ZONE' expression not supported!");
        else if (ctx.DATE() != null) return new SQLDate();
        else if (ctx.DATETIME() != null) return ctx.prec != null ? new SQLDateTime(Integer.parseInt(ctx.prec.getText())) : new SQLDateTime();
        else if (ctx.TIME() != null) return ctx.prec != null ? new SQLTime(Integer.parseInt(ctx.prec.getText())) : new SQLTime();
        else if (ctx.TIMESTAMP() != null) return ctx.prec != null ? new SQLTimestamp(Integer.parseInt(ctx.prec.getText())) : new SQLTimestamp();

        else {
            throw new RuntimeException("Other SQL data types not supported yet!");
        }
    }

    public void visitColumn_Definition_Element(SQLParser.Column_definition_elementContext ctx, SchemaReference schemaReference, String tableName, String attributeName) {
        if (ctx.DEFAULT() != null) builder.setAttributeDefaultExpression(schemaReference, tableName, attributeName, visitExpression(ctx.constant_expr));
        else if (ctx.column_constraint() != null) visitColumn_Constraint(ctx.column_constraint(), schemaReference, tableName, attributeName);
        else throw new RuntimeException("Other column_definition_elements not supported yet!");
    }

    public void visitColumn_Constraint(SQLParser.Column_constraintContext ctx, SchemaReference schemaReference, String tableName, String attributeName) {
        String constraintName = ctx.constraint != null ? ctx.constraint.getText() : unnamedConstraintName + unnamedConstraintNumber++;

        if (ctx.null_notnull() != null) builder.setAttributeNullable(schemaReference, tableName, attributeName, false);
        else if (ctx.PRIMARY() != null) {
            if (ctx.clustered() != null) throw new RuntimeException("PK options not supported yet!");
            if (ctx.primary_key_options().getChildCount() != 0) throw new RuntimeException("PK options not supported yet!");
            builder.addPrimaryKeyConstraint(schemaReference, tableName, constraintName, attributeName);
        } else if (ctx.UNIQUE() != null) {
            if (ctx.clustered() != null) throw new RuntimeException("PK options not supported yet!");
            if (ctx.primary_key_options().getChildCount() != 0) throw new RuntimeException("PK options not supported yet!");
            builder.addUniqueConstraint(schemaReference, tableName, constraintName, attributeName);
        } else if (ctx.foreign_key_options() != null) visitForeign_key_options(ctx.foreign_key_options(), schemaReference, tableName, List.of(attributeName), constraintName);
        else if (ctx.check_constraint() != null)
            builder.addCheckConstraint(schemaReference, tableName, new Check(constraintName, visitCheck_constraint(ctx.check_constraint())));
    }

    public void visitTable_constraint(SQLParser.Table_constraintContext ctx, SchemaReference schemaReference, String tableName) {
        String constraintName = ctx.constraint != null ? ctx.constraint.getText() : unnamedConstraintName + unnamedConstraintNumber++;

        if (ctx.PRIMARY() != null) {
            if (ctx.clustered() != null) throw new RuntimeException("PK options not supported yet!");
            if (ctx.primary_key_options().getChildCount() != 0) throw new RuntimeException("PK options not supported yet!");
            builder.addPrimaryKeyConstraint(schemaReference, tableName, constraintName, visitColumn_name_list_with_order(ctx.column_name_list_with_order()));
        } else if (ctx.UNIQUE() != null) {
            if (ctx.clustered() != null) throw new RuntimeException("PK options not supported yet!");
            if (ctx.primary_key_options().getChildCount() != 0) throw new RuntimeException("PK options not supported yet!");
            builder.addUniqueConstraint(schemaReference, tableName, constraintName, visitColumn_name_list_with_order(ctx.column_name_list_with_order()));
        } else if (ctx.FOREIGN() != null) {
            visitForeign_key_options(ctx.foreign_key_options(), schemaReference, tableName, visitColumn_name_list(ctx.column_name_list()), constraintName);
        } else if (ctx.check_constraint() != null) {
            builder.addCheckConstraint(schemaReference, tableName, new Check(constraintName, visitCheck_constraint(ctx.check_constraint())));
        }
    }

    public List<String> visitColumn_name_list_with_order(SQLParser.Column_name_list_with_orderContext ctx) {
        if (!ctx.ASC().isEmpty() ||!ctx.DESC().isEmpty()) throw new RuntimeException("ASC DESC elements in column_names_list not supported yet!");
        return ctx.id_().stream().map(this::visitId_).toList();
    }

    public BooleanExpression visitCheck_constraint(SQLParser.Check_constraintContext ctx) {
        if (ctx.REPLICATION() != null) throw new RuntimeException("Grammar expression 'NOT FOR REPLICATION' not supported yet!");
        return visitSearch_condition(ctx.search_condition());
    }

    public void visitForeign_key_options(SQLParser.Foreign_key_optionsContext ctx, SchemaReference schemaReference, String tableName, List<String> attributeNames, String constraintName) {
        if (ctx.on_delete() != null || ctx.on_update() != null || ctx.REPLICATION() != null)
            throw new RuntimeException("FK options not supported yet!");
        builder.addForeignKeyConstraint(schemaReference, tableName, constraintName, attributeNames, visitTable_name(ctx.table_name()), visitId_(ctx.table_name().table), visitColumn_name_list(ctx.pk));
    }


    /** NAME/BASIC NODES **/

    public SchemaReference visitFull_table_name(SQLParser.Full_table_nameContext ctx) {
        if (ctx.linkedServer != null) throw new RuntimeException("Grammar expression related to linkedServer in full_table_name not supported yet!");

        if (ctx.schema == null) return defaultSchemaReference;

        return new SchemaReference(
            ctx.server != null ? visitId_(ctx.server) : null,
            ctx.database != null ? visitId_(ctx.database) : null,
            visitId_(ctx.schema));
    }

    public SchemaReference visitSimple_name(SQLParser.Simple_nameContext ctx) {
        if (ctx.schema == null) return defaultSchemaReference;
        return new SchemaReference(visitId_(ctx.schema));
    }

    public ValueExpression visitPrimitive_expression(SQLParser.Primitive_expressionContext ctx) {
        if (ctx.NULL_() != null) {
            //TODO: Future Work - IMPSQL-50
            throw new RuntimeException("Grammar expression of other NULL not supported yet!");
            //return new Null...
        } else if (ctx.primitive_constant() != null) return visitPrimitive_constant(ctx.primitive_constant());
        else {
            throw new RuntimeException("Grammar expression of other primitive expressions not supported yet!");
        }
    }

    public ValueExpression visitPrimitive_constant(SQLParser.Primitive_constantContext ctx) {
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText();
            return new SQLPrimitiveString(str.substring(1,str.length()-1));
        }
        else if (ctx.DECIMAL() != null) return new SQLPrimitiveInteger(Integer.parseInt(ctx.DECIMAL().getText()));
        else if (ctx.FLOAT() != null) return new SQLPrimitiveFloat(Float.parseFloat(ctx.FLOAT().getText()));
        else {
            throw new RuntimeException("Grammar expression of other primitive constants not supported yet!");
        }
    }

    public ComparisonPredicate.ComparisonOperator visitComparison_operator(SQLParser.Comparison_operatorContext ctx) {
        String opString = ctx.getText();
        return switch (opString) {
            case "=" -> ComparisonPredicate.ComparisonOperator.EQ;
            case "<>" -> ComparisonPredicate.ComparisonOperator.NEQ;
            case "<" -> ComparisonPredicate.ComparisonOperator.LT;
            case "<=" -> ComparisonPredicate.ComparisonOperator.LEQ;
            case ">" -> ComparisonPredicate.ComparisonOperator.GT;
            case ">=" -> ComparisonPredicate.ComparisonOperator.GEQ;
            default -> throw new RuntimeException("Grammar expression of different comparison predicates not supported yet!");
        };
    }

    public String visitId_(SQLParser.Id_Context ctx) {
        if (ctx.SQUARE_BRACKET_ID() != null) return ctx.getText().substring(1, ctx.getText().length()-1);
        return ctx.getText();
    }

}
