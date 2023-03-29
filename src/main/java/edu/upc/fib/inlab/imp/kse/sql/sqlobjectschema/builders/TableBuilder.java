package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.builders;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.PrimaryKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Unique;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLDataType;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableBuilder {

    /* TABLE INFO */

    private final String tableName;
    private final SchemaReference schemaReference;

    private final List<String> attributesOrder = new ArrayList<>();
    private final Map<String, ProvisionalAttribute> provisionalAttributeMap = new HashMap<>();

    private final List<Check> checks = new ArrayList<>();

    private final List<String> uniquesOrder = new ArrayList<>();
    private final Map<String, ProvisionalUnique> provisionalUniqueMap = new HashMap<>();

    private final List<String> pksOrder = new ArrayList<>();
    private final Map<String, ProvisionalPrimaryKey> provisionalPrimaryKeyMap = new HashMap<>();

    /* MUTABLE TABLE */

    protected static class MutableTable extends Table {

        protected MutableTable(String tableName, SchemaReference schemaReference, List<Attribute> attributes,
                               List<Check> checkConstraints, List<Unique> uniqueConstraints,
                               List<PrimaryKey> primaryKeyConstraints, List<ForeignKey> foreignKeyConstraints) {
            super(tableName, schemaReference, attributes,
                checkConstraints, uniqueConstraints, primaryKeyConstraints, foreignKeyConstraints);
        }

        protected void addFkConstraint(ForeignKey fk) {
            this.foreignKeyConstraints.add(fk);
        }
    }


    /* PROVISIONAL DOMAIN CLASSES */

    protected static class ProvisionalAttribute {
        private final String attrName;
        private SQLDataType type;
        private boolean isNullable;
        private ValueExpression defaultExpression;

        protected ProvisionalAttribute(String attrName, SQLDataType type, boolean isNullable, ValueExpression defaultExpression) {
            this.attrName = attrName;
            this.type = type;
            this.isNullable = isNullable;
            this.defaultExpression = defaultExpression;
        }

        protected Attribute getAttributeObject() {
            return new Attribute(attrName, type, isNullable, defaultExpression);
        }
    }

    protected static class ProvisionalUnique {
        private final String constraintName;
        private final List<String> uniqueAttributes;

        protected ProvisionalUnique(String constraintName, String attr) {
            this.constraintName = constraintName;
            this.uniqueAttributes = new ArrayList<>();
            this.uniqueAttributes.add(attr);
        }
        protected ProvisionalUnique(String constraintName, List<String> attr) {
            this.constraintName = constraintName;
            this.uniqueAttributes = new ArrayList<>();
            this.uniqueAttributes.addAll(attr);
        }

        protected Unique getUniqueObject(Map<String, Attribute> attributesMap) {
            return new Unique(
                constraintName,
                uniqueAttributes.stream().map(attributesMap::get).toList()
            );
        }
    }

    protected static class ProvisionalPrimaryKey {
        private final String constraintName;
        private final List<String> pkAttributes;

        protected ProvisionalPrimaryKey(String constraintName, String attr) {
            this.constraintName = constraintName;
            this.pkAttributes = new ArrayList<>();
            this.pkAttributes.add(attr);
        }
        protected ProvisionalPrimaryKey(String constraintName, List<String> attr) {
            this.constraintName = constraintName;
            this.pkAttributes = new ArrayList<>();
            this.pkAttributes.addAll(attr);
        }

        protected PrimaryKey getPrimaryKeyObject(Map<String, Attribute> attributesMap) {
            return new PrimaryKey(
                constraintName,
                pkAttributes.stream().map(attributesMap::get).toList()
            );
        }
    }


    /* CONSTRUCTORS */

    protected TableBuilder(String tableName, SchemaReference schemaReference) {
        this.tableName = tableName;
        this.schemaReference = schemaReference;
    }

    protected MutableTable build() {
        if (this.tableName == null) throw new IllegalArgumentException("Table build error. Table name was null.");

        List<Attribute> attributes = new ArrayList<>();
        List<Unique> uniqueConstraints = new ArrayList<>();
        List<PrimaryKey> primaryKeyConstraints = new ArrayList<>();

        Map<String, Attribute> attributesMap = new HashMap<>();
        for (ProvisionalAttribute pa : attributesOrder.stream().map(provisionalAttributeMap::get).toList()) {
            Attribute newAttribute = pa.getAttributeObject();
            attributes.add(newAttribute);
            attributesMap.put(pa.attrName, newAttribute);
        }

        if (attributes.isEmpty()) throw new IllegalArgumentException("Table build error. No attributes found for table.");

        for (ProvisionalUnique pu : uniquesOrder.stream().map(provisionalUniqueMap::get).toList()) {
            uniqueConstraints.add(pu.getUniqueObject(attributesMap));
        }
        for (ProvisionalPrimaryKey ppk : pksOrder.stream().map(provisionalPrimaryKeyMap::get).toList()) {
            primaryKeyConstraints.add(ppk.getPrimaryKeyObject(attributesMap));
        }

        return new MutableTable(
            this.tableName, this.schemaReference, attributes,
            checks, uniqueConstraints, primaryKeyConstraints, new ArrayList<>()
        );
    }


    /* ATTRIBUTES SETTERS */

    protected void addAttribute(String attrName, SQLDataType type, boolean isNullable, ValueExpression defaultExpression) {
        if (provisionalAttributeMap.containsKey(attrName)) throw new SQLObjectAlreadyExistsException("Attribute name already in use.");
        attributesOrder.add(attrName);
        provisionalAttributeMap.put(attrName, new ProvisionalAttribute(attrName, type, isNullable, defaultExpression));
    }


    protected void setAttributeNullable(String attrName, boolean isNullable) {
        ProvisionalAttribute attr = provisionalAttributeMap.get(attrName);
        if (attr == null) throw new MissingReferencedObjectException("Attribute with name '"+attrName+"' not defined.");
        attr.isNullable = isNullable;
    }

    protected void setAttributeType(String attrName, SQLDataType type) {
        ProvisionalAttribute attr = provisionalAttributeMap.get(attrName);
        if (attr == null) throw new MissingReferencedObjectException("Attribute with name '"+attrName+"' not defined.");
        attr.type = type;
    }

    protected void setAttributeDefaultExpression(String attrName, ValueExpression defaultExpression) {
        ProvisionalAttribute attr = provisionalAttributeMap.get(attrName);
        if (attr == null) throw new MissingReferencedObjectException("Attribute with name '"+attrName+"' not defined.");
        attr.defaultExpression = defaultExpression;
    }


    /* CONSTRAINT SETTERS */

    protected void addCheckConstraint(Check checkConstraint) {
        checks.add(checkConstraint);
    }


    protected void addUniqueConstraint(String constraintName, String attrName) {
        if (constraintName == null || attrName == null)
            throw new MissingReferencedObjectException("Some constructor parameter was null (UNIQUE)");
        ProvisionalUnique pu = provisionalUniqueMap.get(constraintName);
        if (pu == null) {
            uniquesOrder.add(constraintName);
            provisionalUniqueMap.put(constraintName, new ProvisionalUnique(constraintName, attrName));
        }
        else pu.uniqueAttributes.add(attrName);
    }
    protected void addUniqueConstraint(String constraintName, List<String> attrNames) {
        if (constraintName == null || attrNames == null || attrNames.isEmpty())
            throw new MissingReferencedObjectException("Some constructor parameter was null or empty (UNIQUE)");
        ProvisionalUnique pu = provisionalUniqueMap.get(constraintName);
        if (pu == null) {
            uniquesOrder.add(constraintName);
            provisionalUniqueMap.put(constraintName, new ProvisionalUnique(constraintName, attrNames));
        }
        else pu.uniqueAttributes.addAll(attrNames);
    }


    protected void addPrimaryKeyConstraint(String constraintName, String attrName) {
        if (constraintName == null || attrName == null)
            throw new IllegalArgumentException("Some constructor parameter was null (PRIMARY KEY)");
        ProvisionalPrimaryKey pk = provisionalPrimaryKeyMap.get(constraintName);
        if (pk == null) {
            pksOrder.add(constraintName);
            provisionalPrimaryKeyMap.put(constraintName, new ProvisionalPrimaryKey(constraintName, attrName));
        }
        else pk.pkAttributes.add(attrName);
        provisionalAttributeMap.get(attrName).isNullable = false;
    }
    protected void addPrimaryKeyConstraint(String constraintName, List<String> attrNames) {
        if (constraintName == null || attrNames == null || attrNames.isEmpty())
            throw new IllegalArgumentException("Some constructor parameter was null or empty (PRIMARY KEY)");
        ProvisionalPrimaryKey pk = provisionalPrimaryKeyMap.get(constraintName);
        if (pk == null) {
            pksOrder.add(constraintName);
            provisionalPrimaryKeyMap.put(constraintName, new ProvisionalPrimaryKey(constraintName, attrNames));
        }
        else pk.pkAttributes.addAll(attrNames);
        attrNames.forEach(a -> provisionalAttributeMap.get(a).isNullable = false);
    }

}
