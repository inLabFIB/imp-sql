package edu.upc.imp.sqlobjectschema;

import edu.upc.imp.printer.SQLServerPrinter;
import edu.upc.imp.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.List;
import java.util.stream.Stream;

public class SQLObjectSchema {
    List<Assertion> assertions;
    List<View> views;

    // Use default visitor: SQLServerPrinter
    List<String> visit() {
        return this.visit(new SQLServerPrinter());
    }

    List<String> visit(SQLObjectSchemaVisitor visitor) {
        return Stream.concat(
            assertions.stream().map(a -> a.visit(visitor)),
            views.stream().map(v -> v.visit(visitor))
        ).toList();
    }
}
