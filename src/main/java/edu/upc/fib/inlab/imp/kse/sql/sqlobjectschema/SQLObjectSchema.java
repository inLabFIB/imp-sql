package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.services.parser.SQLObjectSchemaParser;
import edu.upc.fib.inlab.imp.kse.sql.services.printer.SQLPrinter;
import edu.upc.fib.inlab.imp.kse.sql.services.printer.SQLServerPrinter;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;

import java.util.ArrayList;
import java.util.List;

public class SQLObjectSchema {

    /**
     * Tables should be ordered in a way that any the i-th table does not reference a j-th table if j < i.
     */
    private final List<Table> tables;
    private final List<View> views;
    private final List<Assertion> assertions;

    /** CONSTRUCTORS **/

    public SQLObjectSchema() {
        tables = new ArrayList<>();
        views = new ArrayList<>();
        assertions = new ArrayList<>();
    }

    /** GETTERS **/

    public List<Table> getTables() {
        return new ArrayList<>(tables);
    }

    public List<View> getViews() {
        return new ArrayList<>(views);
    }

    public List<Assertion> getAssertions() {
        return new ArrayList<>(assertions);
    }

    public Table getTable(String tableName, SchemaReference schemaReference) {
        for (Table t : tables) {
            if (t.hasSameIdentifier(tableName, schemaReference)) return t;
        }
        throw new MissingReferencedObjectException("Table with specified info not found.");
    }

    /** MODIFIERS **/

    public void addTable(Table table) {
        for (Table existingTable : tables)
            if (existingTable.hasSameIdentifier(table))
                throw new SQLObjectAlreadyExistsException("Table already exists with same full name.");
        tables.add(table);
    }

    public void addView(View view) {
        for (View existingView : views)
            if (existingView.hasSameIdentifier(view))
                throw new SQLObjectAlreadyExistsException("View already exists with same full name.");
        views.add(view);
    }

    public void addAssertion(Assertion assertion) {
        for (Assertion existingAssertion : assertions)
            if (existingAssertion.hasSameIdentifier(assertion))
                throw new SQLObjectAlreadyExistsException("Assertion already exists with same full name.");
        assertions.add(assertion);
    }

    public void addTables(List<Table> tables) {
        for (Table t : tables) this.addTable(t);
    }

    /** PRINTER **/

    /**
     * @param printer SQL printer which sets a SQL language
     * @return String containing printed TABLES, VIEWS and ASSERTIONS in this order
     */
    public String getPrintedSchemaObjects(SQLPrinter printer) {
        StringBuilder builder = new StringBuilder("");

        if (!getTables().isEmpty()) {
            builder.append("<<TABLES>>\n");
            for (Table t : getTables()) {
                builder.append((String) t.visit(printer)).append("\n");
            }
            builder.append("\n");
        }

        if (!getViews().isEmpty()) {
            builder.append("<<VIEWS>>\n");
            for (View v : getViews()) {
                builder.append((String) v.visit(printer)).append("\n");
            }
            builder.append("\n");
        }

        if (!getAssertions().isEmpty()) {
            builder.append("<<ASSERTIONS>>\n");
            for (Assertion a : getAssertions()) {
                builder.append((String) a.visit(printer)).append("\n");
            }
            builder.append("\n");
        }

        return builder.toString();
    }

    /** OTHER **/

    //TODO: improve this. Avoid calling printer + parser.
    public SQLObjectSchema getCopy() {
        String schemaString = "";
        schemaString += String.join("\n\n", tables.stream().map(s -> s.<String>visit(new SQLServerPrinter())).toList());
        schemaString += String.join("\n\n", views.stream().map(s -> s.<String>visit(new SQLServerPrinter())).toList());
        schemaString += String.join("\n\n", assertions.stream().map(s -> s.<String>visit(new SQLServerPrinter())).toList());

        SQLObjectSchemaParser parser = new SQLObjectSchemaParser();
        parser.parse(schemaString);
        return parser.getSQLObjectSchema();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SQLObjectSchema that = (SQLObjectSchema) o;

        if (!tables.equals(that.tables)) return false;
        if (!views.equals(that.views)) return false;
        return assertions.equals(that.assertions);
    }

    @Override
    public int hashCode() {
        int result = tables.hashCode();
        result = 31 * result + views.hashCode();
        result = 31 * result + assertions.hashCode();
        return result;
    }
}
