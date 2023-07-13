package edu.upc.fib.inlab.imp.kse.sql.core.schema.constraints;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrimaryKey extends TableConstraint {

    private final List<Attribute> pkAttributes;

    public PrimaryKey(String name, List<Attribute> pkAttributes) {
        super(name);
        this.pkAttributes = Objects.requireNonNull(pkAttributes, "The parameter 'pkAttributes' cannot be null.");
    }

    public List<Attribute> getPkAttributes() {
        return new ArrayList<>(pkAttributes);
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PrimaryKey that = (PrimaryKey) o;

        return pkAttributes.equals(that.pkAttributes);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + pkAttributes.hashCode();
        return result;
    }
}
