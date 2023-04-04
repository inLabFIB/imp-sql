package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;

import java.util.List;

public abstract class JoinOperation implements RelationalExpression {

    private final RelationalExpression leftExpression;
    private final RelationalExpression rightExpression;


    protected JoinOperation(RelationalExpression leftExpression, RelationalExpression rightExpression) {
        this.leftExpression = leftExpression;
        this.rightExpression = rightExpression;
    }

    public RelationalExpression getLeftExpression() {
        return leftExpression;
    }

    public RelationalExpression getRightExpression() {
        return rightExpression;
    }

    @Override
    public List<ColumnReference> getOfferedReferences() {
        List<ColumnReference> acc = leftExpression.getOfferedReferences();
        acc.addAll(rightExpression.getOfferedReferences());
        return acc;
    }
}
