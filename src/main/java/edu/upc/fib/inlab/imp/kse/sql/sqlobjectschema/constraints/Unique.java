package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Unique extends TableConstraint {

    private final List<Attribute> attributes;

    public Unique(String name, List<Attribute> attributes) {
        super(name);
        this.attributes = Objects.requireNonNull(attributes, "The parameter 'attributes' cannot be null.");
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<>(attributes);
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Unique unique = (Unique) o;

        if (getName() != null ? !getName().equals(unique.getName()) : unique.getName() != null) return false;
        return attributes.equals(unique.attributes);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + attributes.hashCode();
        return result;
    }
}
