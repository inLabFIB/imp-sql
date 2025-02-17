package edu.upc.fib.inlab.imp.kse.sql.core.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Assertion;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.View;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;

public class SQLObjectSchemaValidator {

    public void validateAliases(Assertion assertion) {
        new AliasValidatorVisitorImpl().validateAssertion(assertion);
    }

    public void validateAliases(View view) {
        new AliasValidatorVisitorImpl().validateView(view);
    }

    public void validateAliases(Query query) {
        new AliasValidatorVisitorImpl().validateQuery(query);
    }

    public void validateAliases(SQLObjectSchema schema) {
        for (Assertion a : schema.getAssertions()) validateAliases(a);
        for (View v : schema.getViews()) validateAliases(v);
    }

}
