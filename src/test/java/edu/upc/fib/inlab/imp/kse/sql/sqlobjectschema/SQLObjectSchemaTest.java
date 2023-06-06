package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLInt;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SQLObjectSchemaTest {

    @Test
    public void addingRepeatedObjectsThrowsException() {
        SQLObjectSchema schema = new SQLObjectSchema();

        SchemaReference sr1 = new SchemaReference("server1", "d1", "s1");
        SchemaReference sr2 = new SchemaReference("server2", "d2", "s2");

        Table table1 = new Table("t1", sr1, List.of(new Attribute("atr1", new SQLInt())));
        View view1 = new View("v1", sr1, new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1)))));
        Assertion assertion1 = new Assertion("a1", sr1, new ComparisonPredicate(
            ComparisonPredicate.ComparisonOperator.EQ,
            new SQLPrimitiveInteger(1),
            new SQLPrimitiveInteger(2)));

        Table table2 = new Table("t1", sr2, List.of(new Attribute("atr1", new SQLInt())));
        View view2 = new View("v1", sr2, new TableExpression(List.of(new AliasableSelectItem(new SQLPrimitiveInteger(1)))));
        Assertion assertion2 = new Assertion("a1", sr2, new ComparisonPredicate(
            ComparisonPredicate.ComparisonOperator.EQ,
            new SQLPrimitiveInteger(1),
            new SQLPrimitiveInteger(2)));

        schema.addTable(table1);
        schema.addView(view1);
        schema.addAssertion(assertion1);

        assertThrows(SQLObjectAlreadyExistsException.class, () -> schema.addTable(table1));
        assertThrows(SQLObjectAlreadyExistsException.class, () -> schema.addView(view1));
        assertThrows(SQLObjectAlreadyExistsException.class, () -> schema.addAssertion(assertion1));

        schema.addTable(table2);
        schema.addView(view2);
        schema.addAssertion(assertion2);

        assertThat(schema.getTables()).hasSize(2);
        assertThat(schema.getViews()).hasSize(2);
        assertThat(schema.getAssertions()).hasSize(2);

    }
}
