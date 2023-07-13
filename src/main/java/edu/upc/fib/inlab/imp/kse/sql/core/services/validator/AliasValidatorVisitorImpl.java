package edu.upc.fib.inlab.imp.kse.sql.core.services.validator;

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
import edu.upc.fib.inlab.imp.kse.sql.core.schema.selection_expressions.SelectItem;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.value_expressions.*;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;
import edu.upc.fib.inlab.imp.kse.sql.core.services.validator.exceptions.*;
import edu.upc.fib.inlab.imp.kse.sql.sql_server.services.printer.SQLServerPrinter;

import java.util.*;


//TODO: Optimize - IMPSQL-49
/**
 * This visitor checks (returns FALSE for) the following cases:
 * - [1] Repeated table aliases in a FROM clause of a TableExpression
 * - [2] Repeated column aliases in a SELECT clause of a TableExpression
 * - [3] Incorrect column references:
 * ---- Non-existent table alias
 * ---- Non-existent column alias for table alias
 * ---- Column references with only column alias has only one possible reference
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

    private boolean isOffered(List<ColumnReference> offered, Set<String> cachedOfferedTableAliases, ColumnReference target) {
        if (target.getTableName() != null) {
            if (!cachedOfferedTableAliases.contains(target.getTableName())) return false;
            return offered.contains(target);
        }
        // No table name, look only one column name. If more than one throw error.
        boolean found = false;
        for (ColumnReference compared : offered) {
            if (compared.getColumnName().equals(target.getColumnName())) {
                if (found) throw new AmbiguousColumnReferenceException("Ambiguous column reference: multiple table aliases offer it.");
                found = true;
            }
        }
        return found;
    }

    @Override
    public Boolean visit(Assertion a) {
        List<ColumnReference> required = a.getBooleanExpression().visit(this);
        if (!required.isEmpty()) {
            String cr = new SQLServerPrinter().visit(required.get(0));
            throw new InvalidColumnReferenceException("The columnReference (" + cr + ") is not a valid reference.");
        }
        return true;
    }

    @Override
    public Boolean visit(View v) {
        List<ColumnReference> required = v.getQuery().visit(this);
        if (!required.isEmpty()) {
            String cr = new SQLServerPrinter().visit(required.get(0));
            throw new InvalidColumnReferenceException("The columnReference (" + cr + ") is not a valid reference.");
        }
        return true;
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

        // Process FROM clause (needs to walk tree since ON clauses need to be checked)
        if (te.getFromClause() != null) {
            required.addAll(te.getFromClause().visit(this));
        }

        // Process FROM clause - OLD
        for (AliasableRelationalExpression a : te.getFromClauseTerminalExpressions()) {
            String relationalExpressionAlias = a.getAlias();
            if (relationalExpressionAlias == null) {
                if (a instanceof TableReference tr)
                    relationalExpressionAlias = tr.getTable().getTableName();
                if (a instanceof Query) throw new NonAliasedFromClauseSubQueryException("Sub-queries in FROM clause must be aliased");
            }
            if (cachedOfferedTableAliases.contains(relationalExpressionAlias)) throw new RepeatedTableAliasException("Repeated table alias (" + relationalExpressionAlias + ").");
            cachedOfferedTableAliases.add(relationalExpressionAlias);
            // Obtain required aliases
            required.addAll(a.visit(this));
            // Obtain offered aliases
            List<ColumnReference> newOffered = a.getOfferedReferences();
            if (newOffered.stream().anyMatch(o -> isOffered(offered, cachedOfferedTableAliases, o)))
                throw new AmbiguousColumnReferenceException("Repeated table.column alias.");
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


    @Override
    public List<ColumnReference> visit(CrossJoin j) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(j.getLeftExpression().visit(this));
        required.addAll(j.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(OnJoin j) {
        List<ColumnReference> offered = j.getOfferedReferences();
        List<ColumnReference> onRequired = j.getOnClause().visit(this);

        //ON CLAUSES shouldn't require upper ColumnReferences ?
        Optional<ColumnReference> cr = onRequired.stream().filter(r -> !offered.contains(r)).findAny();
        if (cr.isPresent()) throw new InvalidOnJoinColumReferenceException("The columnReference (" + new SQLServerPrinter().visit(cr.get()) + ") is not valid for the on clause.");

        List<ColumnReference> required = new ArrayList<>();
//        required.addAll(onRequired.stream().filter(r -> !offered.contains(r)).toList());
        required.addAll(j.getLeftExpression().visit(this));
        required.addAll(j.getRightExpression().visit(this));
        return required;
    }

    @Override
    public <T> T visit(SetOperation so) {
        //TODO: Future Work - IMPSQL-46
        throw new RuntimeException("Validator doesn't work yet with setOperations");
    }

    @Override
    public List<ColumnReference> visit(TableReference tr) {
        return new ArrayList<>();
    }

    @Override
    public List<ColumnReference> visit(ComparisonPredicate cp) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(cp.getLeftExpression().visit(this));
        required.addAll(cp.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(ValueListInPredicate vlip) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(vlip.getMainExpression().visit(this));
        for (ValueExpression ve : vlip.getValueList()) {
            required.addAll(ve.visit(this));
        }
        return required;
    }

    @Override
    public List<ColumnReference> visit(PredicateOperation po) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(po.getLeftExpression().visit(this));
        required.addAll(po.getRightExpression().visit(this));
        return required;
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
    public <T> T visit(SQLCharacter c) {
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
    public <T> T visit(SQLInteger i) {
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

    @Override
    public <T> T visit(SQLNumeric n) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLDateTime dt) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLFunction f) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLVarbit vb) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLDecimal d) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLTime t) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }

    @Override
    public <T> T visit(SQLTimestamp ts) {
        throw new RuntimeException("Visitor shouldn't reach this expression.");
    }
}
