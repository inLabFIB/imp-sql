package edu.upc.imp.sql.sqlobjectschema.constraints;

import edu.upc.imp.sql.sqlobjectschema.Attribute;
import edu.upc.imp.sql.sqlobjectschema.Table;
import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof ForeignKey fk
            && getName().equals(fk.getName())
            && fkAttributes.equals(fk.fkAttributes)
            && pkReferenceTable.hasSameIdentifier(fk.pkReferenceTable)
            && pkReference.equals(fk.pkReference);
    }
}
