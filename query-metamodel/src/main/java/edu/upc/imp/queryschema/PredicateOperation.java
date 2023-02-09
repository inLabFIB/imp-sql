package edu.upc.imp.queryschema;

public class PredicateOperation implements BooleanExpression {
    enum PredicateOperator {
        AND
    }

    private PredicateOperator operator;
}
