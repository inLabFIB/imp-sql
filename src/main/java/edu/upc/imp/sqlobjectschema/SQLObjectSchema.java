package edu.upc.imp.sqlobjectschema;

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

    /*public SQLObjectSchema(SQLObjectSchema oldSchema) {
        this.tables = oldSchema.tables.stream().map(t -> new Table(t, this)).toList();
        this.views = oldSchema.views.stream().map(v -> new View(v, this)).toList();
        this.assertions = oldSchema.assertions.stream().map(a -> new Assertion(a, this)).toList();
    }*/

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

    /** MODIFIERS **/

    public void addTable(Table table) {
        tables.add(table);
    }

    public void addView(View view) {
        views.add(view);
    }

    public void addAssertion(Assertion assertion) {
        assertions.add(assertion);
    }

    /** OTHER **/

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof SQLObjectSchema os
            && assertions.equals(os.assertions)
            && views.equals(os.views)
            && tables.equals(os.tables);
    }
}
