package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableExpression;

public class Validator {

    public boolean validateAliases(TableExpression tableExpression) {
        return new AliasValidatorVisitorImpl().visit(tableExpression);
    }

    public boolean validateAliases(Assertion assertion) {
        //TODO: implemente this
        throw new RuntimeException("Not implemented yet!");
        //return new AliasValidatorVisitorImpl().visit(assertion);
    }

    public boolean validateAliases(SQLObjectSchema schema) {
        //TODO: implemente this
        throw new RuntimeException("Not implemented yet!");
        //return new AliasValidatorVisitorImpl().visit(schema);
    }

}
