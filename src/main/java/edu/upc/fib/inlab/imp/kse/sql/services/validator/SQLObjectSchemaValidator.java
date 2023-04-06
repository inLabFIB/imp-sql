package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.View;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;

import java.util.List;

public class SQLObjectSchemaValidator {

    public boolean validateAliases(Assertion assertion) {
        return assertion.visit(new AliasValidatorVisitorImpl());
    }

    public boolean validateAliases(View view) {
        return view.visit(new AliasValidatorVisitorImpl());
    }

    public boolean validateAliases(SQLObjectSchema schema) {
        // TODO: Instead of returning boolean, return a more descriptive message
        AliasValidatorVisitorImpl aliasValidator = new AliasValidatorVisitorImpl();
        for (Assertion a : schema.getAssertions()) {
            if (!validateAliases(a)) return false;
        }
        for (View v : schema.getViews()) {
            if (!validateAliases(v)) return false;
        }
        return true;
    }

}
