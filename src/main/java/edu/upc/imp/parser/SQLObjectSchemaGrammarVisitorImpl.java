package edu.upc.imp.parser;

import edu.upc.imp.parser.sql_server.TSqlParser;
import edu.upc.imp.sqlobjectschema.*;
import edu.upc.imp.parser.sql_server.TSqlParserBaseVisitor;
import org.antlr.v4.runtime.misc.NotNull;

public class SQLObjectSchemaGrammarVisitorImpl extends TSqlParserBaseVisitor {

    private final SQLObjectSchema schema;

    public SQLObjectSchemaGrammarVisitorImpl(SQLObjectSchema schema) {
        this.schema = schema;
    }

    public Assertion visitCreate_assertion(TSqlParser.Create_assertionContext ctx) {
        String assertionName = visitAssertion_name(ctx.assertion_name());
        BooleanExpression searchCondition = visitAssertion_check(ctx.assertion_check());
        Assertion newAssertion = new Assertion(assertionName, searchCondition);
        schema.addAssertion(newAssertion);
        return newAssertion;
    }

    //Maybe can be removed

    public String visitAssertion_name(TSqlParser.Assertion_nameContext ctx) {
        return visitId_(ctx.assertion);
    }

    //Maybe can be removed

    public BooleanExpression visitAssertion_check(TSqlParser.Assertion_checkContext ctx) {
        return visitSearch_condition(ctx.search_condition());
    }


    public BooleanExpression visitSearch_condition(TSqlParser.Search_conditionContext ctx) {
        if (ctx.AND() != null)  {
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        }
        else if (ctx.OR() != null)  {
            throw new RuntimeException("Grammar expression (`OR`) not supported yet!");
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

    //TODO: finish method

    public Predicate visitPredicate(TSqlParser.PredicateContext ctx) {
        if (ctx.EXISTS() != null) {
            return new ExistsPredicate(null);
        }
        else if (ctx.comparison_operator() != null && ctx.subquery() == null) {
            return new ComparisonPredicate(null,null,null);
        }
        else throw new RuntimeException("Grammar expression of different predicates not supported yet! ");
    }


    /** NAME NODES **/


    public String visitId_(TSqlParser.Id_Context ctx) {
        return ctx.getText();
    }

//
//    public String visitKeyword(TSqlParser.KeywordContext ctx) {
//        return ctx.getText();
//    }
}
