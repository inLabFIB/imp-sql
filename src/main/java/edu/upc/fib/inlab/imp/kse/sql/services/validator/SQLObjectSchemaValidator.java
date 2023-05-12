package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.View;

public class SQLObjectSchemaValidator {

    public void validateAliases(Assertion assertion) {
        assertion.visit(new AliasValidatorVisitorImpl());
    }

    public void validateAliases(View view) {
        view.visit(new AliasValidatorVisitorImpl());
    }

    public void validateAliases(SQLObjectSchema schema) {
        AliasValidatorVisitorImpl aliasValidator = new AliasValidatorVisitorImpl();
        for (Assertion a : schema.getAssertions()) {
            validateAliases(a);
        }
        for (View v : schema.getViews()) {
            validateAliases(v);
        }
    }

}
