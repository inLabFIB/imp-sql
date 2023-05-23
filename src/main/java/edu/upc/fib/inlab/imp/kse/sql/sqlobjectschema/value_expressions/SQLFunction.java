package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

//TODO: future work: improve design of this class considering multiple types and its position in the hierarchy
public class SQLFunction implements ValueExpression {
    private final String functionName;

    private final List<ValueExpression> arguments;
    public SQLFunction(String functionName, List<ValueExpression> arguments) {
        this.functionName = Objects.requireNonNull(functionName, "The parameter 'functionName' cannot be null.");
        this.arguments = arguments;
    }

    public SQLFunction(String functionName) {
        this (functionName, Collections.emptyList());
    }

    @Override
    public String computeDefaultColumnAlias() {
        return null;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<ValueExpression> getArguments() {
        return arguments;
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SQLFunction that)) return false;

        if (!functionName.equals(that.functionName)) return false;
        return Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        int result = functionName.hashCode();
        result = 31 * result + (arguments != null ? arguments.hashCode() : 0);
        return result;
    }
}
