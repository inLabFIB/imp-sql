package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ForeignKey extends TableConstraint {

    private final List<Attribute> fkAttributes;

    private final Table pkReferenceTable;
    private final List<Attribute> pkReference;

    public ForeignKey(String name, List<Attribute> fkAttributes, Table pkReferenceTable, List<Attribute> pkReference) {
        super(name);
        this.fkAttributes = Objects.requireNonNull(fkAttributes, "The parameter 'fkColumns' cannot be null.");
        this.pkReferenceTable = Objects.requireNonNull(pkReferenceTable, "The parameter 'pkReferenceTable' cannot be null.");
        this.pkReference = Objects.requireNonNull(pkReference, "The parameter 'pkReference' cannot be null.");
    }

    public List<Attribute> getFkAttributes() {
        return new ArrayList<>(fkAttributes);
    }

    public Table getPkReferenceTable() {
        return pkReferenceTable;
    }

    public List<Attribute> getPkReference() {
        return new ArrayList<>(pkReference);
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ForeignKey that = (ForeignKey) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (!fkAttributes.equals(that.fkAttributes)) return false;
        if (!pkReferenceTable.equals(that.pkReferenceTable)) return false;
        return pkReference.equals(that.pkReference);
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + fkAttributes.hashCode();
        result = 31 * result + pkReferenceTable.hashCode();
        result = 31 * result + pkReference.hashCode();
        return result;
    }
}
