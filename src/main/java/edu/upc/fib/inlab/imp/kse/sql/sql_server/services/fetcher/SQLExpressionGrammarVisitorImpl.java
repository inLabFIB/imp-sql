package edu.upc.fib.inlab.imp.kse.sql.sql_server.services.fetcher;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.SQLParser;
import edu.upc.fib.inlab.imp.kse.sql.core.services.parser.SQLParserBaseVisitor;

import java.util.List;
import java.util.Objects;

class SQLExpressionGrammarVisitorImpl extends SQLParserBaseVisitor {
    private final String contextTableName;

    public SQLExpressionGrammarVisitorImpl(String tableName) {
        this.contextTableName = tableName;
    }

    @Override
    public ValueExpression visitExpression(SQLParser.ExpressionContext ctx) {
        if (ctx.primitive_expression() != null) return visitPrimitive_expression(ctx.primitive_expression());
        if (ctx.full_column_name() != null)
            return new ColumnReference(this.contextTableName, visitId_(ctx.full_column_name().column_name));
        if (ctx.function_call() != null) return visitFunction_call(ctx.function_call());
        if (ctx.bracket_expression() != null) return visitExpression(ctx.bracket_expression().expression());

        throw new RuntimeException("Expression not supported by grammar.");
    }

    @Override
    public ValueExpression visitPrimitive_expression(SQLParser.Primitive_expressionContext ctx) {
        if (ctx.NULL_() != null) {
            throw new RuntimeException("Grammar expression (`NULL`) not supported yet!");
            //return new Null...
        }
        if (ctx.primitive_constant() != null) return visitPrimitive_constant(ctx.primitive_constant());
        throw new RuntimeException("Expression not supported by grammar.");
    }

    @Override
    public ValueExpression visitPrimitive_constant(SQLParser.Primitive_constantContext ctx) {
        if (ctx.STRING() != null) {
            String str = ctx.STRING().getText();
            return new SQLPrimitiveString(str.substring(1, str.length() - 1));
        }
        if (ctx.DECIMAL() != null) return new SQLPrimitiveInteger(Integer.parseInt(ctx.DECIMAL().getText()));
        if (ctx.FLOAT() != null) return new SQLPrimitiveFloat(Float.parseFloat(ctx.FLOAT().getText()));
        throw new RuntimeException("Expression not supported by grammar.");
    }

    @Override
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

    @Override
    public List<ValueExpression> visitExpression_list(SQLParser.Expression_listContext ctx) {
        return ctx.expression().stream().map(this::visitExpression).toList();
    }

    @Override
    public BooleanExpression visitSearch_condition(SQLParser.Search_conditionContext ctx) {
        if (ctx.AND() != null)
            return new PredicateOperation(
                PredicateOperation.PredicateOperator.AND,
                visitSearch_condition(ctx.search_condition(0)),
                visitSearch_condition(ctx.search_condition(1)));
        if (ctx.OR() != null) {
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

    @Override
    public Predicate visitPredicate(SQLParser.PredicateContext ctx) {
        return new ComparisonPredicate(
            visitComparison_operator(ctx.comparison_operator()),
            visitExpression(ctx.expression(0)),
            visitExpression(ctx.expression(1)));
    }

    @Override
    public ComparisonPredicate.ComparisonOperator visitComparison_operator(SQLParser.Comparison_operatorContext ctx) {
        if (Objects.equals(ctx.getText(), "=")) return ComparisonPredicate.ComparisonOperator.EQ;
        //TODO: V2
        throw new RuntimeException("Grammar expression of different comparison predicates not supported yet!");
    }

    @Override
    public String visitId_(SQLParser.Id_Context ctx) {
        return ctx.getText();
    }
}
