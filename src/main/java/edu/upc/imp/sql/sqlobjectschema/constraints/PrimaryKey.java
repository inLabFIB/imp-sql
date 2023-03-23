package edu.upc.imp.sql.sqlobjectschema.constraints;

import edu.upc.imp.sql.sqlobjectschema.Attribute;
import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof PrimaryKey pk
            && getName().equals(pk.getName())
            && pkAttributes.equals(pk.pkAttributes);
    }
}
