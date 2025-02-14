package edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.boolean_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;

public interface SQLObjectSchemaVisitor<T> {
    T visit(TableExpression te);

    T visit(CrossJoin j);

    T visit(OnJoin j);

    T visit(TableReference tr);

    T visit(ComparisonPredicate cp);

    T visit(ColumnReference cr);

    T visit(PredicateOperation po);

    T visit(Assertion a);

    T visit(View v);

    T visit(NotOperation no);

    T visit(ExistsPredicate ep);

    T visit(SQLPrimitiveInteger d);

    T visit(SQLPrimitiveFloat f);

    T visit(SQLPrimitiveString s);

    T visit(Asterisk a);

    T visit(AliasableSelectItem asi);

    T visit(Table t);

    T visit(SchemaReference sr);

    T visit(Attribute a);

    T visit(Check c);

    T visit(Unique u);

    T visit(PrimaryKey pk);

    T visit(ForeignKey fk);

    T visit(SQLCharacter c);

    T visit(SQLVarchar v);

    T visit(SQLBit b);

    T visit(SQLInteger i);

    T visit(SQLSmallint s);

    T visit(SQLFloat f);

    T visit(SQLReal r);

    T visit(SQLDate d);

    T visit(SQLDoublePrecision dp);

    T visit(SQLNumeric n);

    T visit(SQLDateTime dt);

    T visit(SQLFunction f);

    T visit(ValueListInPredicate vlip);

    T visit(SetOperation so);

    T visit(SQLVarbit vb);

    T visit(SQLDecimal d);

    T visit(SQLTime t);

    T visit(SQLTimestamp ts);
}
