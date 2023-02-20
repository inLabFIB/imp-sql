package edu.upc.imp.parser;

import edu.upc.imp.parser.sql_server.TSqlParser;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.parser.sql_server.TSqlParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

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
        String assertionName = visitAssertion_name(ctx.assertion_name());
        BooleanExpression searchCondition = visitAssertion_check(ctx.assertion_check());
        Assertion newAssertion = new Assertion(assertionName, searchCondition);
        schema.addAssertion(newAssertion);
        return newAssertion;
    }

    //TODO: Maybe can be removed
    public String visitAssertion_name(TSqlParser.Assertion_nameContext ctx) {
        return visitId_(ctx.assertion);
    }

    //TODO: Maybe can be removed
    public BooleanExpression visitAssertion_check(TSqlParser.Assertion_checkContext ctx) {
        return visitSearch_condition(ctx.search_condition());
    }


    public Query visitSelect_statement_standalone(TSqlParser.Select_statement_standaloneContext ctx)  {
        if (ctx.with_expression() != null) throw new RuntimeException("Grammar expression (`WITH`) not supported yet!");
        return visitSelect_statement(ctx.select_statement());
    }

    public Query visitSelect_statement(TSqlParser.Select_statementContext ctx) {
        if (ctx.select_order_by_clause() != null) throw new RuntimeException("Grammar expression (`ORDER BY`) not supported yet!");
        else if (ctx.for_clause() != null) throw new RuntimeException("Grammar expression (`FOR ...`) not supported yet!");
        else if (ctx.option_clause() != null) throw new RuntimeException("Grammar expression (`OPTION ...`) not supported yet!");
        else return visitQuery_expression(ctx.query_expression());
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

            if (ctx.NOT() != null) return new NotOperation(expression);
            else return expression;
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
        else if(ctx.full_column_name() != null) return visitFull_column_name(ctx.full_column_name());
        else if(ctx.bracket_expression() != null) return visitBracket_expression(ctx.bracket_expression());
        else {
            //TODO: V2
            throw new RuntimeException("Grammar expression of other expressions not supported yet!");
        }
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

    //TODO: drama with UNIONS
    public Query visitQuery_expression(TSqlParser.Query_expressionContext ctx) {
        return null;
    }

    public TableExpression visitQuerySpecification(TSqlParser.Query_specificationContext ctx) {
        if (ctx.allOrDistinct != null) throw new RuntimeException("Grammar expression (`ALL/DISTINCT`) not supported yet!");
        if(ctx.top != null) throw new RuntimeException("Grammar expression (`TOP`) not supported yet!");
        if(ctx.INTO() != null) throw new RuntimeException("Grammar expression (`TOP`) not supported yet!");
        if(ctx.GROUP() != null) throw new RuntimeException("Grammar expression (`GROUP BY`) not supported yet!");
        if(ctx.HAVING() != null) throw new RuntimeException("Grammar expression (`HAVING`) not supported yet!");

        List<SelectItem> selectClause = visitSelect_list(ctx.columns);
        RelationalExpression fromClause = null;
        BooleanExpression whereClause = null;

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
        if (ctx.eq != null) return new AliasableSelectItem(ctx.leftAlias.getText(), visitExpression(ctx.leftAssignment));
        else return new AliasableSelectItem(visitAs_column_alias(ctx.as_column_alias()), visitExpression(ctx.leftAssignment));
    }
    public String visitAs_column_alias(TSqlParser.As_column_aliasContext ctx) {
        return ctx.column_alias().getText();
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
            //TODO: implement this
            throw new RuntimeException("implement this!");
        }
        return root;
    }

    public TableExpression visitTable_source_item(TSqlParser.Table_source_itemContext ctx) {
        return null;
    }

    public Query visitSubquery(TSqlParser.SubqueryContext ctx) {
        return visitSelect_statement(ctx.select_statement());
    }


    /** NAME/BASIC NODES **/

    public FullTableName visitFull_table_name(TSqlParser.Full_table_nameContext ctx) {
        if (ctx.linkedServer != null) {
            throw new RuntimeException("Grammar expression related to linkedServer in full_table_name not supported yet!");

        } else {
            return new FullTableName(
                visitId_(ctx.server), visitId_(ctx.database), visitId_(ctx.schema), visitId_(ctx.table));
        }
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
        if (ctx.STRING() != null) return new SQLString(ctx.STRING().getText());
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


//    public String visitKeyword(TSqlParser.KeywordContext ctx) {
//        return ctx.getText();
//    }
}
