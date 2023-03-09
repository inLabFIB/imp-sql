package edu.upc.imp.fetcher.sql_server;
import edu.upc.imp.parser.sql_server.TSqlExpressionParserBaseVisitor;
import edu.upc.imp.sqlobjectschema.boolean_expressions.*;
import edu.upc.imp.sqlobjectschema.value_expressions.*;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.parser.sql_server.TSqlExpressionParser;

import java.util.Objects;

public class TSQLExpressionGrammarVisitorImpl  extends TSqlExpressionParserBaseVisitor {
    private final String contextTableName;
    private ValueExpression valueExpression;
    private BooleanExpression booleanExpression;

    public TSQLExpressionGrammarVisitorImpl(String tableName) {
        this.contextTableName = tableName;
    }

    public ValueExpression getValueExpression() {
        return valueExpression;
    }
    public BooleanExpression getBooleanExpression() {
        return booleanExpression;
    }

    public SQLObjectSchemaEntity visitExpression(TSqlExpressionParser.ExpressionContext ctx) {
        if (ctx.default_expression() != null) {
            this.valueExpression = visitDefault_expression(ctx.default_expression());
            return this.valueExpression;
        }
        if (ctx.search_condition() != null) {
            this.booleanExpression = visitSearch_condition(ctx.search_condition());
            return this.booleanExpression;
        }
        throw new RuntimeException("Expression not supported by grammar.");
    }

    public ValueExpression visitDefault_expression(TSqlExpressionParser.Default_expressionContext ctx) {
        if (ctx.primitive_expression() != null) return visitPrimitive_expression(ctx.primitive_expression());
        if (ctx.default_expression() != null) return visitDefault_expression(ctx.default_expression());
        throw new RuntimeException("Expression not supported by grammar.");
    }

    public ValueExpression visitPrimitive_expression(TSqlExpressionParser.Primitive_expressionContext ctx) {
        if (ctx.NULL_() != null) {
            throw new RuntimeException("Grammar expression (`NULL`) not supported yet!");
            //return new Null...
        }
        if (ctx.primitive_constant() != null) return visitPrimitive_constant(ctx.primitive_constant());
        throw new RuntimeException("Expression not supported by grammar.");
    }

    public ValueExpression visitPrimitive_constant(TSqlExpressionParser.Primitive_constantContext ctx) {
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText();
            return new SQLPrimitiveString(str.substring(1,str.length()-1));
        }
        if (ctx.DECIMAL() != null) return new SQLPrimitiveInteger(Integer.parseInt(ctx.DECIMAL().getText()));
        if (ctx.FLOAT() != null) return new SQLPrimitiveFloat(Float.parseFloat(ctx.FLOAT().getText()));
        throw new RuntimeException("Expression not supported by grammar.");
    }

    public BooleanExpression visitSearch_condition(TSqlExpressionParser.Search_conditionContext ctx) {
        if (ctx.AND() != null)
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        if (ctx.OR() != null)  {
            throw new RuntimeException("Grammar expression (`OR`) not supported yet!");
            //TODO: V2
            /*return new PredicateOperation(
                PredicateOperation.PredicateOperator.OR,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));*/
        }

        BooleanExpression expression;
        if (ctx.predicate() != null) expression = visitPredicate(ctx.predicate());
        else expression = visitSearch_condition(ctx.search_condition(0));

        for (int i = 0; i < ctx.NOT().size(); i++) {
            expression = new NotOperation(expression);
        }
        return expression;
    }

    public Predicate visitPredicate(TSqlExpressionParser.PredicateContext ctx) {
        return new ComparisonPredicate(
            visitComparison_operator(ctx.comparison_operator()),
            visitCheck_value_expression(ctx.check_value_expression(0)),
            visitCheck_value_expression(ctx.check_value_expression(1)));
    }

    public ComparisonPredicate.ComparisonOperator visitComparison_operator(TSqlExpressionParser.Comparison_operatorContext ctx) {
        if (Objects.equals(ctx.getText(), "=")) return ComparisonPredicate.ComparisonOperator.EQ;
        //TODO: V2
        throw new RuntimeException("Grammar expression of different comparison predicates not supported yet!");
    }

    public ValueExpression visitCheck_value_expression(TSqlExpressionParser.Check_value_expressionContext ctx) {
        if (ctx.primitive_expression() != null) return visitPrimitive_expression(ctx.primitive_expression());
        if (ctx.column_name != null) return new ColumnReference(this.contextTableName, visitId_(ctx.column_name));
        if (ctx.check_value_expression() != null) return visitCheck_value_expression(ctx.check_value_expression());
        throw new RuntimeException("Expression not supported by grammar.");
    }

    public String visitId_(TSqlExpressionParser.Id_Context ctx) {
        return ctx.getText();
    }
}
