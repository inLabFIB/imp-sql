package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class SQLObjectSchema {

    private final List<Assertion> assertions;
    private final List<View> views;

    /** CONSTRUCTORS **/

    public SQLObjectSchema() {
        assertions = new ArrayList<>();
        views = new ArrayList<>();
    }


    /** GETTERS **/

    public List<Assertion> getAssertions() {
        return new ArrayList<>(assertions);
    }

    public List<View> getViews() {
        return new ArrayList<>(views);
    }

    /** MODIFIERS **/

    public void addAssertion(Assertion assertion) {
        assertions.add(assertion);
    }


//    // Use default visitor: SQLServerPrinter
//    List<String> visit() {
//        return this.visit(new SQLServerPrinter());
//    }

//    List<String> visit(SQLObjectSchemaVisitor visitor) {
//        return Stream.concat(
//            assertions.stream().map(a -> a.visit(visitor)),
//            views.stream().map(v -> v.visit(visitor))
//        ).toList();
//    }
}
