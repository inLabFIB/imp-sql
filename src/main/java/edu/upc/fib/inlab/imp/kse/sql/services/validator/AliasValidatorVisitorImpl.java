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
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.relational_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.AliasableSelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.Asterisk;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ColumnReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveFloat;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveInteger;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.SQLPrimitiveString;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This visitor checks (returns FALSE for) the following cases:
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
    public Boolean visit(Assertion a) {
        try {
            List<ColumnReference> required = a.getBooleanExpression().visit(this);
            return required.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<ColumnReference> visit(NotOperation no) {
        return no.getExpression().visit(this);
    }

    @Override
    public List<ColumnReference> visit(ExistsPredicate ep) {
        return ep.getQuery().visit(this);
    }

    @Override
    public List<ColumnReference> visit(TableExpression te) {
        List<ColumnReference> required = new ArrayList<>();
        List<ColumnReference> offered = new ArrayList<>();
        Set<String> cachedOfferedTableAliases = new HashSet<>();

        // Process FROM clause
        for (AliasableRelationalExpression a : te.getFromClauseTerminalExpressions()) {
            String relationalExpressionAlias = a.getAlias();
            if (relationalExpressionAlias == null) {
                if (a instanceof TableReference tr)
                    relationalExpressionAlias = tr.getTable().getTableName();
                if (a instanceof Query) throw new RuntimeException("Sub-queries in FROM clause must be aliased");
            }
            if (cachedOfferedTableAliases.contains(relationalExpressionAlias)) throw new RuntimeException("Repeated table alias.");
            cachedOfferedTableAliases.add(relationalExpressionAlias);
            // Obtain required aliases
            required.addAll(a.visit(this));
            // Obtain offered aliases
            List<ColumnReference> newOffered = a.getOfferedReferences();
            if (newOffered.stream().anyMatch(o -> isOffered(offered, cachedOfferedTableAliases, o)))
                throw new RuntimeException("Repeated table.column alias.");
            offered.addAll(newOffered);
        }

        // Process SELECT clause
        if (te.getSelectClause() != null) {
            for (SelectItem s : te.getSelectClause()) {
                for (ColumnReference cr : s.<List<ColumnReference>>visit(this)) {
                    if (!isOffered(offered, cachedOfferedTableAliases, cr)) {
                        required.add(cr);
                    }
                }
            }
        }

        // Process WHERE clause
        if (te.getWhereClause() != null) {
            for (ColumnReference cr : te.getWhereClause().<List<ColumnReference>>visit(this)) {
                if (!isOffered(offered, cachedOfferedTableAliases, cr)) {
                    required.add(cr);
                }
            }
        }

        return required;
    }

    private boolean isOffered(List<ColumnReference> offered, Set<String> cachedOfferedTableAliases, ColumnReference target) {
        if (target.getTableName() != null) {
            if (!cachedOfferedTableAliases.contains(target.getTableName())) return false;
            return offered.contains(target);
        }
        // No table name, look only one column name. If more than one throw error.
        boolean found = false;
        for (ColumnReference compared : offered) {
            if (compared.getColumnName().equals(target.getColumnName())) {
                if (found) throw new RuntimeException("Ambiguous column reference: multiple table aliases offer it.");
                found = true;
            }
        }
        return found;
    }


    @Override
    public List<ColumnReference> visit(CrossJoin j) {
        List<ColumnReference> left = j.getLeftExpression().visit(this);
        List<ColumnReference> right = j.getRightExpression().visit(this);
        left.addAll(right);
        return left;
    }

    @Override
    public List<ColumnReference> visit(OnJoin j) {
        List<ColumnReference> offered = j.getOfferedReferences();
        List<ColumnReference> onRequired = j.getOnClause().visit(this);

        List<ColumnReference> required = new ArrayList<>();
        required.addAll(onRequired.stream().filter(r -> !offered.contains(r)).toList());
        required.addAll(j.getLeftExpression().visit(this));
        required.addAll(j.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(TableReference tr) {
        return new ArrayList<>();
    }

    @Override
    public List<ColumnReference> visit(ComparisonPredicate cp) {
        List<ColumnReference> left = cp.getLeftExpression().visit(this);
        List<ColumnReference> right = cp.getRightExpression().visit(this);
        left.addAll(right);
        return left;
    }

    @Override
    public List<ColumnReference> visit(PredicateOperation po) {
        List<ColumnReference> left = po.getLeftExpression().visit(this);
        List<ColumnReference> right = po.getRightExpression().visit(this);
        left.addAll(right);
        return left;
    }

    @Override
    public List<ColumnReference> visit(Asterisk a) {
        return new ArrayList<>(); // It returns the available offered aliases. So no more should be necessary.
    }

    @Override
    public List<ColumnReference> visit(AliasableSelectItem asi) {
        return asi.getExpression().visit(this);
    }

    @Override
    public List<ColumnReference> visit(ColumnReference cr) {
        return List.of(cr);
    }

    @Override
    public List<ColumnReference> visit(SQLPrimitiveInteger d) {
        return new ArrayList<>();
    }

    @Override
    public List<ColumnReference> visit(SQLPrimitiveFloat f) {
        return new ArrayList<>();
    }

    @Override
    public List<ColumnReference> visit(SQLPrimitiveString s) {
        return new ArrayList<>();
    }

    //TODO:V2

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
    public <T> T visit(SchemaReference sr) {
        return null;
    }

    @Override
    public <T> T visit(Table t) {
        return null;
    }

    @Override
    public <T> T visit(Attribute a) {
        return null;
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
