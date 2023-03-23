package edu.upc.imp.sql.sqlobjectschema;

import edu.upc.imp.sql.sqlobjectschema.constraints.*;
import edu.upc.imp.sql.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaEntity;
import edu.upc.imp.sql.sqlobjectschema.visitor.SQLObjectSchemaVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    /** Syntactic equals implementation **/
    @Override
    public boolean equals(Object o) {
        return o instanceof Table t
            && tableName.equals(t.tableName)
            && Objects.equals(schemaReference,t.schemaReference)
            && attributes.equals(t.attributes)
            && checkConstraints.equals(t.checkConstraints)
            && uniqueConstraints.equals(t.uniqueConstraints)
            && primaryKeyConstraints.equals(t.primaryKeyConstraints)
            && foreignKeyConstraints.equals(t.foreignKeyConstraints);
    }

    public boolean hasSameIdentifier(Table t) {
        return tableName.equals(t.tableName)
            && Objects.equals(schemaReference, t.schemaReference);
    }

    public boolean hasSameIdentifier(String tableName, SchemaReference schemaReference) {
        return this.tableName.equalsIgnoreCase(tableName)
            && Objects.equals(this.schemaReference, schemaReference);
    }
}
