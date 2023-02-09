package edu.upc.imp.queryschema;

public class ComparisonPredicate extends Predicate {
    enum ComparisonOperator {
        EQ
    }

    private ComparisonOperator operator;
    private ValueExpression leftExpression;
    private ValueExpression rightExpression;
}
