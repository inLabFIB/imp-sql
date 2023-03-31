package edu.upc.fib.inlab.imp.kse.sql.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ComparisonPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.ExistsPredicate;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.NotOperation;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.boolean_expressions.PredicateOperation;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.CrossJoin;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.OnJoin;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableExpression;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.TableReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveFloat;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveString;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

/**
 * This visitor checks( returns FALSE for) the following cases:
 * - [1] Repeated table aliases in a FROM clause of a TableExpression
 * - [2] Repeated column aliases in a SELECT clause of a TableExpression
 * - [3] Incorrect column references:
 * ---- Non-existent table alias
 * ---- Non-existent column alias for table alias
 * ---- Column references with only column alias has only one possible reference
 *
 * Special cases to think about:
 * - Table Aliases:
 * ---- The original table name can't be referenced. Only the table alias.
 * - Identical Table aliases:
 * ---- It is permitted if they are in different levels.
 * ---- If they are referenced from a sub query inside a NOT EXISTS clause, for example, that grants visibility
 * ---- over multiple identical aliases, only one will be "referencable", with priority of the closest level to the one
 * ---- containing the column reference.

 */
public class AliasValidatorVisitorImpl implements SQLObjectSchemaVisitor {

    @Override
    public Boolean visit(TableExpression te) {
        // TODO: Visit TableExpressions of sub-queries in the FROM context recursively.
        //  (rec call, returns boolean, doesn't need passed down variables)

        // TODO: Obtain aliases generated in FROM clause

        // TODO: Check there are no collisions among those aliases
        //  - [1] No two different terminal aliasable relational expressions have the same alias
        //  - [2] No two column aliases are repeated within the same aliasable relational expression

        // TODO: Add new stack element (ColumnReference list)

        // TODO: Visit SELECT items + WHERE clause and obtain used ColumnReferences

        // TODO: Check used are either in new FROM-aliases or in previous stack elements
        //  - Note: ColumnReferences with null table name must search all possible aliases and check exactly one
        //    is found with the same column name (if 0 are found it is incorrect, if >1 then ambiguous alias).

        // TODO: Pop stack element
        return null;
    }


    @Override
    public <T> T visit(CrossJoin j) {
        return null;
    }

    @Override
    public <T> T visit(OnJoin j) {
        return null;
    }

    @Override
    public <T> T visit(TableReference tr) {
        // Table references are only found in FROM clause, they introduce more possible aliases

        // TODO: Return a ColumnReference of tr tableName.ColumnName for each of the columns.
        //  Unless tr is aliased, then return tableAlias.ColumnName for each of the columns.
        return null;
    }

    @Override
    public <T> T visit(ComparisonPredicate cp) {
        return null;
    }

    @Override
    public <T> T visit(ColumnReference cr) {
        return null;
    }

    @Override
    public <T> T visit(PredicateOperation po) {
        return null;
    }

    @Override
    public <T> T visit(NotOperation no) {
        return null;
    }

    @Override
    public <T> T visit(ExistsPredicate ep) {
        return null;
    }

    @Override
    public <T> T visit(Asterisk a) {
        return null;
    }

    @Override
    public <T> T visit(AliasableSelectItem asi) {
        return null;
    }

    @Override
    public <T> T visit(Table t) {
        return null;
    }

    @Override
    public <T> T visit(SchemaReference sr) {
        return null;
    }

    @Override
    public <T> T visit(Attribute a) {
        return null;
    }


    //TODO:V2

    @Override
    public <T> T visit(Assertion a) {
        return null;
    }


    @Override
    public <T> T visit(View v) {
        return null;
    }

    @Override
    public <T> T visit(Check c) {
        return null;
    }

    @Override
    public <T> T visit(Unique u) {
        return null;
    }

    @Override
    public <T> T visit(PrimaryKey pk) {
        return null;
    }

    @Override
    public <T> T visit(ForeignKey fk) {
        return null;
    }


    /* NON REACHABLE EXPRESSIONS */

    @Override
    public <T> T visit(SQLPrimitiveInteger d) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLPrimitiveFloat f) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLPrimitiveString s) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLChar c) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLVarchar v) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLBit b) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLInt i) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLSmallint s) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLFloat f) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLReal r) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLDate d) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLDoublePrecision dp) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }
}
