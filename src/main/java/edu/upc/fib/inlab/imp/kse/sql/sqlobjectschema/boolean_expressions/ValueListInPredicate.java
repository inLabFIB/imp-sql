package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ValueListInPredicate extends InPredicate {

    private final List<ValueExpression> valueList;

    public ValueListInPredicate(ValueExpression mainExpression, List<ValueExpression> valueList) {
        super(mainExpression);
        this.valueList = Objects.requireNonNull(valueList, "The parameter 'valueList' cannot be null.");
    }

    public List<ValueExpression> getValueList() {
        return new ArrayList<>(valueList);
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

        ValueListInPredicate that = (ValueListInPredicate) o;

        return Objects.equals(valueList, that.valueList);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (valueList != null ? valueList.hashCode() : 0);
        return result;
    }
}
