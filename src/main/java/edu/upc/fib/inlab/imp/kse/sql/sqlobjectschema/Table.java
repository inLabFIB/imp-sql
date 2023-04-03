package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.*;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.*;

public class Table implements SQLObjectSchemaEntity {

    private final String tableName;
    /**
     * NULLABLE
     */
    private final SchemaReference schemaReference;
    private final List<Attribute> attributes;

    protected final List<Check> checkConstraints;
    protected final List<Unique> uniqueConstraints;
    protected final List<PrimaryKey> primaryKeyConstraints;

    protected final List<ForeignKey> foreignKeyConstraints;

    public Table(String tableName, SchemaReference schemaReference, List<Attribute> attributes,
                 List<Check> checkConstraints, List<Unique> uniqueConstraints,
                 List<PrimaryKey> primaryKeyConstraints, List<ForeignKey> foreignKeyConstraints) {
        this.tableName = Objects.requireNonNull(tableName, "The parameter 'tableName' cannot be null.");
        this.schemaReference = schemaReference;
        this.attributes = Objects.requireNonNull(attributes, "The parameter 'attributes' cannot be null.");
        this.checkConstraints = Objects.requireNonNull(checkConstraints, "The parameter 'checkConstraints' cannot be null.");
        this.uniqueConstraints = Objects.requireNonNull(uniqueConstraints, "The parameter 'uniqueConstraints' cannot be null.");
        this.primaryKeyConstraints = Objects.requireNonNull(primaryKeyConstraints, "The parameter 'primaryKeyConstraints' cannot be null.");
        this.foreignKeyConstraints = Objects.requireNonNull(foreignKeyConstraints, "The parameter 'foreignKeyConstraints' cannot be null.");

        Set<String> seenAttributes = new HashSet<>();
        this.attributes.forEach(a -> seenAttributes.add(a.getName()));
        if (seenAttributes.size() < this.attributes.size()) throw new RuntimeException("Repeated attribute names in table definition");
    }

    public Table(String tableName, SchemaReference schemaReference, List<Attribute> attributes) {
        this(tableName, schemaReference, attributes, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public Table(String tableName, List<Attribute> attributes) {
        this(tableName, null, attributes, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    public String getTableName() {
        return tableName;
    }

    public SchemaReference getSchemaReference() {
        return schemaReference;
    }

    public List<Attribute> getAttributes() {
        return new ArrayList<>(attributes);
    }

    public List<TableConstraint> getTableConstraints() {
        List<TableConstraint> constraints = new ArrayList<>();
        constraints.addAll(checkConstraints);
        constraints.addAll(uniqueConstraints);
        constraints.addAll(primaryKeyConstraints);
        constraints.addAll(foreignKeyConstraints);
        return constraints;
    }

    public List<ForeignKey> getForeignKeyConstraints() {
        return foreignKeyConstraints;
    }

    public Attribute getAttribute(String attributeName) {
        for (Attribute a : attributes) {
            if (a.getName().equals(attributeName)) return a;
        }
        throw new MissingReferencedObjectException("Attribute name not found in table.");
    }

    @Override
    public <T> T visit(SQLObjectSchemaVisitor visitor) {
        return visitor.visit(this);
    }




    public boolean hasSameIdentifier(Table t) {
        return tableName.equals(t.tableName)
            && Objects.equals(schemaReference, t.schemaReference);
    }

    public boolean hasSameIdentifier(String tableName, SchemaReference schemaReference) {
        return this.tableName.equalsIgnoreCase(tableName)
            && Objects.equals(this.schemaReference, schemaReference);
    }

    //FIXME: try to check equality of classes
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Table table)/*|| getClass() != o.getClass()*/) return false;
        //Needs to be modified because of the existence of MutableTables

        if (!tableName.equals(table.tableName)) return false;
        if (!Objects.equals(schemaReference, table.schemaReference))
            return false;
        if (!attributes.equals(table.attributes)) return false;
        if (!checkConstraints.equals(table.checkConstraints)) return false;
        if (!uniqueConstraints.equals(table.uniqueConstraints)) return false;
        if (!primaryKeyConstraints.equals(table.primaryKeyConstraints)) return false;
        return foreignKeyConstraints.equals(table.foreignKeyConstraints);
    }

    @Override
    public int hashCode() {
        int result = tableName.hashCode();
        result = 31 * result + (schemaReference != null ? schemaReference.hashCode() : 0);
        result = 31 * result + attributes.hashCode();
        result = 31 * result + checkConstraints.hashCode();
        result = 31 * result + uniqueConstraints.hashCode();
        result = 31 * result + primaryKeyConstraints.hashCode();
        result = 31 * result + foreignKeyConstraints.hashCode();
        return result;
    }
}
