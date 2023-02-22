package edu.upc.imp.parser;

import edu.upc.imp.parser.sql_server.TSqlParser;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.parser.sql_server.TSqlParserBaseVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SQLObjectSchemaGrammarVisitorImpl extends TSqlParserBaseVisitor {

    private final SQLObjectSchema schema;

    public SQLObjectSchemaGrammarVisitorImpl(SQLObjectSchema schema) {
        this.schema = schema;
    }

    /** TOP LEVEL STATEMENTS NODES **/

    public Assertion visitCreate_assertion(TSqlParser.Create_assertionContext ctx) {
        FullTableName assertionName = visitSimple_name(ctx.simple_name());
        BooleanExpression searchCondition = visitAssertion_check(ctx.assertion_check());
        Assertion newAssertion = new Assertion(assertionName, searchCondition);
        schema.addAssertion(newAssertion);
        return newAssertion;
    }

    public BooleanExpression visitAssertion_check(TSqlParser.Assertion_checkContext ctx) {
        return visitSearch_condition(ctx.search_condition());
    }


    public Query visitSelect_statement_standalone(TSqlParser.Select_statement_standaloneContext ctx)  {
        if (ctx.with_expression() != null) throw new RuntimeException("Grammar expression (`WITH`) not supported yet!");
        Query selectStatement =  visitSelect_statement(ctx.select_statement());
        if (ctx.getParent() instanceof TSqlParser.Dml_clauseContext) {
            selectStatement = selectStatement.getFirstLevelCopy();
            schema.addSelect(selectStatement);
        }
        return selectStatement;
    }

    public Query visitSelect_statement(TSqlParser.Select_statementContext ctx) {
        if (ctx.select_order_by_clause() != null) throw new RuntimeException("Grammar expression (`ORDER BY`) not supported yet!");
        if (ctx.for_clause() != null) throw new RuntimeException("Grammar expression (`FOR ...`) not supported yet!");
        if (ctx.option_clause() != null) throw new RuntimeException("Grammar expression (`OPTION ...`) not supported yet!");
        return visitQuery_expression(ctx.query_expression());
    }

    public View visitCreate_view(TSqlParser.Create_viewContext ctx) {
        if (ctx.WITH().size() > 0) throw new RuntimeException("Grammar expression (`WITH ...`) in create view not supported yet!");

        List<String> columnNames = null;
        if (ctx.column_name_list() != null) columnNames = visitColumn_name_list(ctx.column_name_list());
        View view = new View(
            visitSimple_name(ctx.simple_name()),
            columnNames,
            visitSelect_statement_standalone(ctx.select_statement_standalone()));
        schema.addView(view);
        return view;
    }

    public List<String> visitColumn_name_list(TSqlParser.Column_name_listContext ctx) {
        return ctx.col.stream().map(this::visitId_).toList();
    }

    /** BOOLEAN EXPRESSION / ASSERTION NODES **/


    public BooleanExpression visitSearch_condition(TSqlParser.Search_conditionContext ctx) {
        if (ctx.AND() != null)  {
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        }
        else if (ctx.OR() != null)  {
            throw new RuntimeException("Grammar expression (`OR`) not supported yet!");
            //TODO: V2
            /*return new PredicateOperation(
                PredicateOperation.PredicateOperator.OR,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));*/
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

    public Predicate visitPredicate(TSqlParser.PredicateContext ctx) {
        if (ctx.EXISTS() != null) return new ExistsPredicate(visitSubquery(ctx.subquery()));
        else if (ctx.comparison_operator() != null && ctx.subquery() == null) {
            return new ComparisonPredicate(
                visitComparison_operator(ctx.comparison_operator()),
                visitExpression(ctx.expression(0)),
                visitExpression(ctx.expression(1)));
        } else {
            //TODO: V2
            throw new RuntimeException("Grammar expression of different predicates not supported yet!");
        }
    }

    /** VALUE EXPRESSION NODES **/

    public ValueExpression visitExpression(TSqlParser.ExpressionContext ctx) {
        if (ctx.primitive_expression() != null) return visitPrimitive_expression(ctx.primitive_expression());
        if(ctx.full_column_name() != null) return visitFull_column_name(ctx.full_column_name());
        if(ctx.bracket_expression() != null) return visitBracket_expression(ctx.bracket_expression());

        //TODO: V2
        throw new RuntimeException("Grammar expression of other expressions not supported yet!");
    }

    public ValueExpression visitBracket_expression(TSqlParser.Bracket_expressionContext ctx) {
        if (ctx.expression() != null) return visitExpression(ctx.expression());
        else return visitSubquery(ctx.subquery());
    }

    public ColumnReference visitFull_column_name(TSqlParser.Full_column_nameContext ctx) {
        FullTableName fullTableName = null;

        if (ctx.DELETED() != null || ctx.INSERTED() != null || ctx.IDENTITY() != null || ctx.ROWGUID() != null) {
            //TODO: V2
            throw new RuntimeException("Grammar expression related to extra info for full_column_name not supported yet!");
        }
        else if (ctx.full_table_name() != null) {
            fullTableName = visitFull_table_name(ctx.full_table_name());
        }

        return new ColumnReference(fullTableName, visitId_(ctx.column_name));
    }

    /** QUERY NODES **/

    public Query visitQuery_expression(TSqlParser.Query_expressionContext ctx) {
        if (ctx.select_order_by_clause() != null) throw new RuntimeException("Grammar expression (`ORDER BY`) not supported yet!");
        if (ctx.UNION() != null) {
            //TODO: V2
            throw new RuntimeException("UNIONS not supported yet!");
        }
        if (ctx.unions.size() != 0) {
            //TODO: V2
            throw new RuntimeException("UNIONS not supported yet!");
        }
        return visitQuerySpecification(ctx.query_specification());
    }

    public TableExpression visitQuerySpecification(TSqlParser.Query_specificationContext ctx) {
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

    public List<SelectItem> visitSelect_list(TSqlParser.Select_listContext ctx) {
        List<SelectItem> columns = new ArrayList<>();
        for (TSqlParser.Select_list_elemContext item : ctx.selectElement) {
            columns.add(visitSelect_list_elem(item));
        }
        return columns;
    }

    public SelectItem visitSelect_list_elem(TSqlParser.Select_list_elemContext ctx) {
        if (ctx.udt_elem() != null) throw new RuntimeException("Grammar expression (udt elements) not supported yet!");
        if (ctx.LOCAL_ID() != null) throw new RuntimeException("Grammar expression (Local id variables) not supported yet!");

        if (ctx.asterisk() != null) return visitAsterisk(ctx.asterisk());
        else return visitExpression_elem(ctx.expression_elem());

    }

    public Asterisk visitAsterisk(TSqlParser.AsteriskContext ctx) {
        if (ctx.table_name() != null
            || ctx.INSERTED() != null
            || ctx.DELETED() != null) throw new RuntimeException("Grammar expression (`TABLE.*`) not supported yet!");
        return new Asterisk();
    }

    //TODO: store more information of the original sql statement (equality/ as / implicit as)

    public AliasableSelectItem visitExpression_elem(TSqlParser.Expression_elemContext ctx) {
        if (ctx.eq != null) return new AliasableSelectItem(visitExpression(ctx.leftAssignment), ctx.leftAlias.getText());
        else {
            String alias = null;
            if (ctx.as_column_alias() != null) alias = visitAs_column_alias(ctx.as_column_alias());
            return new AliasableSelectItem(visitExpression(ctx.expressionAs), alias);
        }
    }

    public String visitAs_table_alias(TSqlParser.As_table_aliasContext ctx) {
        return ctx.table_alias().getText();
    }

    public String visitAs_column_alias(TSqlParser.As_column_aliasContext ctx) {
        return visitColumn_alias(ctx.column_alias());
    }

    public String visitColumn_alias(TSqlParser.Column_aliasContext ctx) {
        if (ctx.STRING() != null) throw new RuntimeException("Grammar expression (STRING) in alias expressions not supported yet!");
        return visitId_(ctx.id_());
    }


    public RelationalExpression visitTable_sources(TSqlParser.Table_sourcesContext ctx) {
        if (ctx.non_ansi_join() != null) return visitNon_ansi_join(ctx.non_ansi_join());

        RelationalExpression root = visitTable_source(ctx.source.get(0));
        for (int i = 1; i < ctx.source.size(); i++) {
            root = new CrossJoin(root, visitTable_source(ctx.source.get(i)));
        }
        return root;
    }

    public RelationalExpression visitNon_ansi_join(TSqlParser.Non_ansi_joinContext ctx) {
        RelationalExpression root = visitTable_source(ctx.source.get(0));
        for (int i = 1; i < ctx.source.size(); i++) {
            root = new CrossJoin(root, visitTable_source(ctx.source.get(i)));
        }
        return root;
    }

    public RelationalExpression visitTable_source(TSqlParser.Table_sourceContext ctx) {
        RelationalExpression root = visitTable_source_item(ctx.table_source_item());
        for (TSqlParser.Join_partContext join_part : ctx.join_part()) {
            RelationalExpression rightExpression = visitJoin_part(join_part);
            if (join_part.join_on() != null) {
                BooleanExpression onCondition = visitSearch_condition(join_part.join_on().search_condition());
                if (join_part.join_on().inner != null)
                    root = new OnJoin(OnJoin.JoinOperator.INNER, root, rightExpression, onCondition);
                else throw new RuntimeException("outer/left/right/full joins not supported yet!");
            }
            else if (join_part.cross_join() != null) {
                root = new CrossJoin(root, rightExpression);
            }
        }
        return root;
    }

    public RelationalExpression visitJoin_part(TSqlParser.Join_partContext ctx) {
        if (ctx.apply_() != null) throw new RuntimeException("apply_ joins not supported yet!");
        if (ctx.pivot() != null || ctx.unpivot() != null) throw new RuntimeException("pivot joins not supported yet!");

        if (ctx.join_on() != null) return visitJoin_on(ctx.join_on());
        if (ctx.cross_join() != null) return visitCross_join(ctx.cross_join());

        throw new RuntimeException("Unknown join operation");
    }

    /**
     * On condition not processed in upper visitors.
     */
    public RelationalExpression visitJoin_on(TSqlParser.Join_onContext ctx) {
        if (ctx.join_hint != null) throw new RuntimeException("join_hint not supported yet!");

        //TODO: V2
        if (ctx.join_type != null || ctx.outer != null) throw new RuntimeException("outer/left/right/full joins not supported yet!");

        return visitTable_source(ctx.source);
    }

    public RelationalExpression visitCross_join(TSqlParser.Cross_joinContext ctx) {
        return visitTable_source_item(ctx.table_source_item());
    }

    public RelationalExpression visitTable_source_item(TSqlParser.Table_source_itemContext ctx) {
        if (ctx.column_alias_list() != null) throw new RuntimeException("column_alias_list not supported yet!");

        String alias = null;
        if (ctx.as_table_alias() != null) alias = visitAs_table_alias(ctx.as_table_alias());

        if (ctx.full_table_name() != null) {
            if (ctx.deprecated_table_hint() != null
                || ctx.with_table_hints() != null
                || ctx.sybase_legacy_hints() != null) throw new RuntimeException("Grammar expression related to table_source_item not supported yet!");

            return new TableReference(visitFull_table_name(ctx.full_table_name()), alias);
        }
        if (ctx.derived_table() != null) return visitDerived_table(ctx.derived_table()).getAliasedCopy(alias);
        if (ctx.table_source() != null) return visitTable_source(ctx.table_source());

        throw new RuntimeException("Other table_source_item types not supported yet!");
    }

    public Query visitDerived_table(TSqlParser.Derived_tableContext ctx) {
        if (ctx.table_value_constructor() != null) throw new RuntimeException("Grammar expression table_value_constructor not supported yet!");

        Query root = visitSubquery(ctx.subquery().get(0));
        //TODO: V2
        if (ctx.subquery().size() > 1) throw new RuntimeException("UNIONS not supported yet!");
//        for (int i = 1; i < ctx.subquery().size(); i++) {
//            root = new (root, visitTable_source(ctx.source.get(i)));
//        }
        return root;
    }

    public Query visitSubquery(TSqlParser.SubqueryContext ctx) {
        return visitSelect_statement(ctx.select_statement());
    }


    /** NAME/BASIC NODES **/

    public FullTableName visitFull_table_name(TSqlParser.Full_table_nameContext ctx) {
        if (ctx.linkedServer != null) throw new RuntimeException("Grammar expression related to linkedServer in full_table_name not supported yet!");

        return new FullTableName(
            ctx.server != null ? visitId_(ctx.server) : null,
            ctx.database != null ? visitId_(ctx.database) : null,
            ctx.schema != null ? visitId_(ctx.schema) : null,
            visitId_(ctx.table));
    }

    public FullTableName visitSimple_name(TSqlParser.Simple_nameContext ctx) {
        return new FullTableName(
            ctx.schema != null ? visitId_(ctx.schema) : null,
            visitId_(ctx.name));
    }

    public ValueExpression visitPrimitive_expression(TSqlParser.Primitive_expressionContext ctx) {
        if (ctx.NULL_() != null) {
            //TODO: V2
            throw new RuntimeException("Grammar expression of other NULL not supported yet!");
            //return new Null...
        } else if (ctx.primitive_constant() != null) return visitPrimitive_constant(ctx.primitive_constant());
        else {
            //TODO: V2
            throw new RuntimeException("Grammar expression of other primitive expressions not supported yet!");
        }
    }

    public ValueExpression visitPrimitive_constant(TSqlParser.Primitive_constantContext ctx) {
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText();
            return new SQLString(str.substring(1,str.length()-1));
        }
        else if (ctx.DECIMAL() != null) return new SQLInteger(Integer.parseInt(ctx.DECIMAL().getText()));
        else if (ctx.FLOAT() != null) return new SQLFloat(Float.parseFloat(ctx.FLOAT().getText()));
        else {
            //TODO: V2
            throw new RuntimeException("Grammar expression of other primitive constants not supported yet!");
        }
    }

    public ComparisonPredicate.ComparisonOperator visitComparison_operator(TSqlParser.Comparison_operatorContext ctx) {
        String opString = ctx.getText();
        if (Objects.equals(opString, "=")) return ComparisonPredicate.ComparisonOperator.EQ;
        else {
            //TODO: V2
            throw new RuntimeException("Grammar expression of different comparison predicates not supported yet!");
        }
    }

    public String visitId_(TSqlParser.Id_Context ctx) {
        return ctx.getText();
    }
}
