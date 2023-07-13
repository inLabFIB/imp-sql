package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;

import java.util.List;
import java.util.Objects;

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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoinOperation that = (JoinOperation) o;

        if (!Objects.equals(leftExpression, that.leftExpression))
            return false;
        return Objects.equals(rightExpression, that.rightExpression);
    }

    @Override
    public int hashCode() {
        int result = leftExpression != null ? leftExpression.hashCode() : 0;
        result = 31 * result + (rightExpression != null ? rightExpression.hashCode() : 0);
        return result;
    }
}
