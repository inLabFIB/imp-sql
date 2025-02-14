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

    public enum SetOperator {
        UNION,
        EXCEPT,
        INTERSECT
    }

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
    public List<ColumnReference> getOfferedReferences() {
        // TODO: Future Work - IMPSQL-46
        throw new IMPSqlException("OfferedReferences of set operations not defined yet!");
    }

    @Override
    public String computeDefaultColumnAlias() {
        return getAlias();
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
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
}
