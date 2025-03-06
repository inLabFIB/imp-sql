package edu.upc.fib.inlab.imp.kse.sql.core.services.validator;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
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
 * <ol>
 *     <li>Repeated table aliases in a FROM clause of a TableExpression</li>
 *     <li>Repeated column aliases in a SELECT clause of a TableExpression</li>
 *     <li>Incorrect column references:</li>
 *     <ul>
 *         <li>Non-existent table alias</li>
 *         <li>Non-existent column alias for table alias</li>
 *         <li>Column references with only column alias has only one possible reference</li>
 *     </ul>
 * </ol>
 * Special cases to think about:
 * <ul>
 *     <li>Table Aliases:</li>
 *     <ul>
 *         <li>The original table name can't be referenced. Only the table alias.</li>
 *     </ul>
 *     <li>Identical Table aliases:</li>
 *     <ul>
 *         <li>It is permitted if they are in different levels.</li>
 *         <li>If they are referenced from a sub query inside a NOT EXISTS clause, for example, that grants visibility
 *         over multiple identical aliases, only one will be "referencable", with priority of the closest level to the
 *         one containing the column reference.</li>
 *     </ul>
 * </ul>
 */
public class AliasValidatorVisitorImpl implements SQLObjectSchemaVisitor<List<ColumnReference>> {

    public static final String VISITOR_EXCEPTION_MESSAGE = "Visitor shouldn't reach this expression.";

    public void validateAssertion(Assertion a) {
        List<ColumnReference> required = a.getBooleanExpression().visit(this);
        if (!required.isEmpty()) {
            String cr = new SQLServerPrinter().visit(required.get(0));
            throw new InvalidColumnReferenceException(cr);
        }
    }

    public void validateView(View v) {
        validateQuery(v.getQuery());
    }

    public void validateQuery(Query q) {
        List<ColumnReference> required = q.visit(this);
        if (!required.isEmpty()) {
            String cr = new SQLServerPrinter().visit(required.get(0));
            throw new InvalidColumnReferenceException(cr);
        }
    }

    @Override
    public List<ColumnReference> visit(TableExpression te) {
        List<ColumnReference> required = new ArrayList<>();
        List<ColumnReference> offered = new ArrayList<>();
        Set<String> cachedOfferedTableAliases = new HashSet<>();

        // Process FROM clause (needs to walk tree since ON clauses need to be checked)
        required.addAll(visitFromClause(te));
        // Process FROM clause - OLD
        required.addAll(processFromClauseTerminalExpressions(te,offered, cachedOfferedTableAliases));
        // Process SELECT clause
        required.addAll(visitSelectClause(te, offered, cachedOfferedTableAliases));
        // Process WHERE clause
        required.addAll(visitWhereClause(te, offered, cachedOfferedTableAliases));

        return required;
    }

    private List<ColumnReference> visitFromClause(TableExpression te) {
        List<ColumnReference> required = new ArrayList<>();
        if (te.getFromClause() != null) {
            required.addAll(te.getFromClause().visit(this));
        }
        return required;
    }

    private List<ColumnReference> processFromClauseTerminalExpressions(TableExpression te, List<ColumnReference> offered, Set<String> cachedOfferedTableAliases) {
        List<ColumnReference> required = new ArrayList<>();
        for (AliasableRelationalExpression a : te.getFromClauseTerminalExpressions()) {
            String relationalExpressionAlias = getRelationalExpressionAlias(a);
            if (cachedOfferedTableAliases.contains(relationalExpressionAlias)) {
                throw new RepeatedTableAliasException("Repeated table alias (" + relationalExpressionAlias + ").");
            }
            cachedOfferedTableAliases.add(relationalExpressionAlias);
            // Obtain required aliases
            required.addAll(a.visit(this));
            // Obtain offered aliases
            List<ColumnReference> newOffered = a.getOfferedReferences();
            if (newOffered.stream().anyMatch(o -> isOffered(offered, cachedOfferedTableAliases, o)))
                throw new AmbiguousColumnReferenceException("Repeated table.column alias.");
            offered.addAll(newOffered);
        }
        return required;
    }

    private String getRelationalExpressionAlias(AliasableRelationalExpression a) {
        String relationalExpressionAlias = a.getAlias();
        if (relationalExpressionAlias == null) {
            if (a instanceof TableReference tr) {
                relationalExpressionAlias = tr.getTableSource().getName();
            }
            if (a instanceof Query) {
                throw new NonAliasedFromClauseSubQueryException("Sub-queries in FROM clause must be aliased");
            }
        }
        return relationalExpressionAlias;
    }

    private List<ColumnReference> visitSelectClause(TableExpression te, List<ColumnReference> offered, Set<String> cachedOfferedTableAliases) {
        List<ColumnReference> required = new ArrayList<>();
        if (te.getSelectClause() != null) {
            for (SelectItem s : te.getSelectClause()) {
                for (ColumnReference cr : s.visit(this)) {
                    if (!isOffered(offered, cachedOfferedTableAliases, cr)) {
                        required.add(cr);
                    }
                }
            }
        }
        return required;
    }

    private List<ColumnReference> visitWhereClause(TableExpression te, List<ColumnReference> offered, Set<String> cachedOfferedTableAliases) {
        List<ColumnReference> required = new ArrayList<>();
        if (te.getWhereClause() != null) {
            for (ColumnReference cr : te.getWhereClause().visit(this)) {
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
                if (found)
                    throw new AmbiguousColumnReferenceException("Ambiguous column reference: multiple table aliases offer it.");
                found = true;
            }
        }
        return found;
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
        if (cr.isPresent())
            throw new InvalidOnJoinColumReferenceException("The columnReference (" + new SQLServerPrinter().visit(cr.get()) + ") is not valid for the on clause.");

        List<ColumnReference> required = new ArrayList<>();
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
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(cp.getLeftExpression().visit(this));
        required.addAll(cp.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(ColumnReference cr) {
        return List.of(cr);
    }

    @Override
    public List<ColumnReference> visit(PredicateOperation po) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(po.getLeftExpression().visit(this));
        required.addAll(po.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(Assertion a) {
        throw new IMPSqlException("Visitor shouldn't reach this expression. Use validateAssertion method.");
    }

    @Override
    public List<ColumnReference> visit(View v) {
        throw new IMPSqlException("Visitor shouldn't reach this expression. Use validateView method.");
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
    public List<ColumnReference> visit(Asterisk a) {
        return new ArrayList<>(); // It returns the available offered aliases. So no more should be necessary.
    }

    @Override
    public List<ColumnReference> visit(AliasableSelectItem asi) {
        return asi.getExpression().visit(this);
    }

    @Override
    public List<ColumnReference> visit(Table t) {
        return Collections.emptyList();
    }

    /* NON REACHABLE EXPRESSIONS */
    @Override
    public List<ColumnReference> visit(SchemaReference sr) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(Attribute a) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(Check c) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(Unique u) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(PrimaryKey pk) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(ForeignKey fk) {
        return Collections.emptyList();
    }

    @Override
    public List<ColumnReference> visit(SQLCharacter c) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLVarchar v) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLBit b) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLInteger i) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLSmallint s) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLFloat f) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLReal r) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLDate d) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLDoublePrecision dp) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLNumeric n) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLDateTime dt) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLFunction f) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(ValueListInPredicate vlip) {
        List<ColumnReference> required = new ArrayList<>(vlip.getMainExpression().visit(this));
        for (ValueExpression ve : vlip.getValueList()) required.addAll(ve.visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(SetOperation so) {
        List<ColumnReference> required = new ArrayList<>();
        required.addAll(so.getLeftExpression().visit(this));
        required.addAll(so.getRightExpression().visit(this));
        return required;
    }

    @Override
    public List<ColumnReference> visit(SQLVarbit vb) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLDecimal d) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLTime t) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }

    @Override
    public List<ColumnReference> visit(SQLTimestamp ts) {
        throw new IMPSqlException(VISITOR_EXCEPTION_MESSAGE);
    }
}
