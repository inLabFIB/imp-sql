package edu.upc.imp.parser;

import edu.upc.imp.parser.sql_server.TSqlParser;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.parser.sql_server.TSqlParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

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

    //TODO:
    public Query visitSelect_statement(TSqlParser.Select_statementContext ctx) {return null;}

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

    //TODO: finish this
    public Query visitQuery_expression(TSqlParser.Query_expressionContext ctx) {
        if (ctx.UNION() != null || ctx.unions != null) {
            //TODO: V2
            throw new RuntimeException("Grammar expression (`WITH`) not supported yet!");
        }
        return null;
    }

    public TableExpression visitQuerySpecification(TSqlParser.Query_specificationContext ctx) {return null;}


    public Query visitSubquery(TSqlParser.SubqueryContext ctx) {
        return visitSelect_statement(ctx.select_statement());
    }

    //TODO:
    public TableExpression visitQuery_specification(TSqlParser.Query_specificationContext ctx) {
        return null;
    }

    /*public SetOperation visitSql_union(TSqlParser.Sql_unionContext ctx) {
        return null;
    }*/



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
