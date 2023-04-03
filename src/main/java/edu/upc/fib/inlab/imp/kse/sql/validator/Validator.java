package edu.upc.fib.inlab.imp.kse.sql.validator;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;

import java.util.List;

public class Validator {

    public boolean validateAliases(TableExpression tableExpression) {
        List<ColumnReference> required = tableExpression.visit(new AliasValidatorVisitorImpl());
        return required.isEmpty();
    }

    public boolean validateAliases(Assertion assertion) {
        return assertion.visit(new AliasValidatorVisitorImpl());
    }

    public boolean validateAliases(SQLObjectSchema schema) {
        //TODO: implemente this
        throw new RuntimeException("Not implemented yet!");
        //return new AliasValidatorVisitorImpl().visit(schema);
    }

}
