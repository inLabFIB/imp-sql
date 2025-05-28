package edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.List;
import java.util.Objects;

/**
 * It is assumed that both expressions have same arity of offered terms and same domain for each variable ordered.
 */
public class SetOperation extends Query {

    private final SetOperator operator;
    private final boolean all;
    private final Query leftExpression;
    private final Query rightExpression;
    public SetOperation(SetOperator operator, boolean all, Query leftExpression, Query rightExpression) {
        this(operator, all, leftExpression, rightExpression, null);
    }

    public SetOperation(SetOperator operator, boolean all, Query leftExpression, Query rightExpression, String alias) {
        super(alias);
        this.operator = Objects.requireNonNull(operator, "The parameter 'operator' cannot be null.");
        this.leftExpression = Objects.requireNonNull(leftExpression, "The parameter 'leftExpression' cannot be null.");
        this.rightExpression = Objects.requireNonNull(rightExpression, "The parameter 'rightExpression' cannot be null.");
        this.all = all;

        checkQueriesAreCompatible();
    }

    /**
     * This method checks:
     * <ol>
     *     <li>Checks both queries has same number of return columns.</li>
     *     <li>TODO: IMPSQL-56 Check SetOperation query return column type matching</li>
     * </ol>
     */
    private void checkQueriesAreCompatible() {
        int leftReturns = this.leftExpression.getNumberOfReturnColumns();
        int rightReturns = this.rightExpression.getNumberOfReturnColumns();
        if (leftReturns != rightReturns)
            throw new IMPSqlException("SELECTS to the left and right of UNION do not have the same number of result columns! (" + leftReturns + ", " + rightReturns + ")");
    }

    public SetOperator getOperator() {
        return operator;
    }

    public Query getLeftExpression() {
        return leftExpression;
    }

    public Query getRightExpression() {
        return rightExpression;
    }

    public boolean returnsDuplicates() {
        return all;
    }

    @Override
    public AliasableRelationalExpression getAliasedCopy(String newAlias) {
        return new SetOperation(operator, all, leftExpression, rightExpression, newAlias);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SetOperation that = (SetOperation) o;

        if (operator != that.operator) return false;
        if (!leftExpression.equals(that.leftExpression)) return false;
        return rightExpression.equals(that.rightExpression);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + operator.hashCode();
        result = 31 * result + leftExpression.hashCode();
        result = 31 * result + rightExpression.hashCode();
        return result;
    }

    /**
     * Only references from the left expression are used (this means that references from the right expression are
     * invalid from this set operation upwards).
     */
    @Override
    public List<ColumnReference> getOfferedReferences() {
        return this.leftExpression.getOfferedReferences().stream()
            .map(cr -> new ColumnReference(getAlias(), cr.getColumnName()))
            .toList();
    }

    @Override
    public String computeDefaultColumnAlias() {
        return getAlias();
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public int getNumberOfReturnColumns() {
        return this.leftExpression.getNumberOfReturnColumns();
    }

    public enum SetOperator {
        UNION,
        EXCEPT,
        INTERSECT
    }
}
