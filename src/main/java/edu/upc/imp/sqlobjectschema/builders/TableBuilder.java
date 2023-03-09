package edu.upc.imp.sqlobjectschema.builders;

import edu.upc.imp.sqlobjectschema.Attribute;
import edu.upc.imp.sqlobjectschema.SchemaReference;
import edu.upc.imp.sqlobjectschema.Table;
import edu.upc.imp.sqlobjectschema.constraints.*;
import edu.upc.imp.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.imp.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.imp.sqlobjectschema.sql_data_types.SQLDataType;
import edu.upc.imp.sqlobjectschema.value_expressions.ValueExpression;

import java.util.*;

public class TableBuilder {

    //TODO: try to follow a builder pattern (avoid getter)

    //TODO: store list for each map/set to avoid losing order of constraint and attribute orders

    /* TABLE INFO */
    private String tableName;
    private SchemaReference schemaReference;

    private final Map<String, ProvisionalAttribute> provisionalAttributeMap = new HashMap<>();

    private final List<Check> checks = new ArrayList<>();
    private final Map<String, ProvisionalDefault> provisionalDefaultMap = new HashMap<>();
    private final Map<String, ProvisionalUnique> provisionalUniqueMap = new HashMap<>();
    private final Map<String, ProvisionalPrimaryKey> provisionalPrimaryKeyMap = new HashMap<>();
    private final Map<String, ProvisionalForeignKey> provisionalForeignKeyMap = new HashMap<>();

    /* ATTRIBUTES */
    public class ProvisionalAttribute {
        private final String attrName;
        private final int attrPosition;
        private SQLDataType type;
        private boolean isNullable;

        public ProvisionalAttribute(String attrName, int attrPosition, SQLDataType type, boolean isNullable) {
            this.attrName = attrName;
            this.attrPosition = attrPosition;
            this.type = type;
            this.isNullable = isNullable;
        }

        public int getAttrPosition() {
            return attrPosition;
        }

        public Attribute getAttributeObject() {
            return new Attribute(attrName, type, isNullable);
        }
    }

    /* CONSTRAINTS */
    public class ProvisionalDefault {
        private final String constraintName;
        private final String attributeName;
        private final ValueExpression expression;

        public ProvisionalDefault(String constraintName, String attributeName, ValueExpression expression) {
            this.constraintName = constraintName;
            this.attributeName = attributeName;
            this.expression = expression;
        }

        public Default getDefaultObject(Map<String, Attribute> attributesMap) {
            return new Default(
                constraintName,
                attributesMap.get(attributeName),
                expression
            );
        }
    }

    public class ProvisionalUnique {
        private final String constraintName;
        private final Set<String> uniqueAttributes;

        public ProvisionalUnique(String constraintName, String attr) {
            this.constraintName = constraintName;
            this.uniqueAttributes = new HashSet<>();
            this.uniqueAttributes.add(attr);
        }
        public ProvisionalUnique(String constraintName, List<String> attr) {
            this.constraintName = constraintName;
            this.uniqueAttributes = new HashSet<>();
            this.uniqueAttributes.addAll(attr);
        }

        public Unique getUniqueObject(Map<String, Attribute> attributesMap) {
            return new Unique(
                constraintName,
                uniqueAttributes.stream().map(attributesMap::get).toList()
            );
        }
    }

    public class ProvisionalPrimaryKey {
        private final String constraintName;
        private final Set<String> pkAttributes;

        public ProvisionalPrimaryKey(String constraintName, String attr) {
            this.constraintName = constraintName;
            this.pkAttributes = new HashSet<>();
            this.pkAttributes.add(attr);
        }
        public ProvisionalPrimaryKey(String constraintName, List<String> attr) {
            this.constraintName = constraintName;
            this.pkAttributes = new HashSet<>();
            this.pkAttributes.addAll(attr);
        }

        public PrimaryKey getPrimaryKeyObject(Map<String, Attribute> attributesMap) {
            return new PrimaryKey(
                constraintName,
                pkAttributes.stream().map(attributesMap::get).toList()
            );
        }
    }

    public class ProvisionalForeignKey {
        private final String constraintName;
        private final List<String> fkAttributes;
        private final String referencedTable;
        private final List<String> referencedAttr;

        public ProvisionalForeignKey(String constraintName, String attr, String refTable, String refAttr) {
            this.constraintName = constraintName;
            this.fkAttributes = new ArrayList<>();
            this.fkAttributes.add(attr);
            this.referencedTable = refTable;
            this.referencedAttr = new ArrayList<>();
            this.referencedAttr.add(refAttr);
        }
        public ProvisionalForeignKey(String constraintName, List<String> attr, String refTable, List<String> refAttr) {
            this.constraintName = constraintName;
            this.fkAttributes = new ArrayList<>();
            this.fkAttributes.addAll(attr);
            this.referencedTable = refTable;
            this.referencedAttr = new ArrayList<>();
            this.referencedAttr.addAll(refAttr);
        }

        public ForeignKey getForeignKeyObject(Map<String, Attribute> attributesMap, List<Table> referencableTables) {
            Table refTable = referencableTables.stream()
                .filter(t -> t.getTableName().equalsIgnoreCase(this.referencedTable))
                .findFirst().orElseThrow(() -> new MissingReferencedObjectException("Foreign Key reference not found!"));
            List<Attribute> refAttributes = new ArrayList<>();
            for (String ref : this.referencedAttr) {
                refAttributes.add(refTable.getAttributes()
                    .stream().filter(a -> a.getName().equals(ref))
                    .findFirst().orElseThrow(() -> new MissingReferencedObjectException("Foreign Key reference not found!")));
            }
            return new ForeignKey(
                constraintName,
                fkAttributes.stream().map(attributesMap::get).toList(),
                refAttributes
            );
        }
    }


    /* CONSTRUCTORS */

    public TableBuilder() {}
    public TableBuilder(String tableName) {
        this(tableName, null);
    }
    public TableBuilder(String tableName, SchemaReference schemaReference) {
        this.tableName = tableName;
        this.schemaReference = schemaReference;
    }

    /* GETTERS */

    public String getTableName() {
        return tableName;
    }

    public List<String> getTableReferences() {
        return this.provisionalForeignKeyMap.values().stream().map(f -> f.referencedTable).distinct().toList();
    }

    public Table build(List<Table> referencableTables) {
        if (this.tableName == null) throw new IllegalArgumentException("Table build error. Table name was null.");

        List<Attribute> attributes = new ArrayList<>();
        List<Default> defaultConstraints = new ArrayList<>();
        List<Unique> uniqueConstraints = new ArrayList<>();
        List<PrimaryKey> primaryKeyConstraints = new ArrayList<>();
        List<ForeignKey> foreignKeyConstraints = new ArrayList<>();

        Map<String, Attribute> attributesMap = new HashMap<>();
        for (ProvisionalAttribute pa : this.provisionalAttributeMap.values().stream()
            .sorted(Comparator.comparingInt(ProvisionalAttribute::getAttrPosition)).toList()) {
            Attribute newAttribute = pa.getAttributeObject();
            attributes.add(newAttribute);
            attributesMap.put(pa.attrName, newAttribute);
        }

        if (attributes.isEmpty()) throw new IllegalArgumentException("Table build error. No attributes found for table.");

        for (ProvisionalDefault pd : this.provisionalDefaultMap.values()) {
            defaultConstraints.add(pd.getDefaultObject(attributesMap));
        }
        for (ProvisionalUnique pu : this.provisionalUniqueMap.values()) {
            uniqueConstraints.add(pu.getUniqueObject(attributesMap));
        }
        for (ProvisionalPrimaryKey ppk : this.provisionalPrimaryKeyMap.values()) {
            primaryKeyConstraints.add(ppk.getPrimaryKeyObject(attributesMap));
        }
        for (ProvisionalForeignKey pfk : this.provisionalForeignKeyMap.values()) {
            foreignKeyConstraints.add(pfk.getForeignKeyObject(attributesMap, referencableTables));
        }

        return new Table(
            this.tableName, this.schemaReference, attributes,
            checks, defaultConstraints, uniqueConstraints, primaryKeyConstraints, foreignKeyConstraints
        );
    }

    /* SETTERS */

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setSchemaReference(SchemaReference schemaReference) {
        this.schemaReference = schemaReference;
    }

    /* - For attributes */

    public void addAttribute(String attrName) {
        addAttribute(attrName, provisionalAttributeMap.values().size()+1, true, null);
    }

    public void addAttribute(String attrName, SQLDataType type) {
        addAttribute(attrName, provisionalAttributeMap.values().size()+1, true, type);
    }

    public void addAttribute(String attrName, boolean isNullable) {
        addAttribute(attrName, provisionalAttributeMap.values().size()+1, isNullable, null);
    }

    public void addAttribute(String attrName, int attrPosition, boolean isNullable, SQLDataType type) {
        if (provisionalAttributeMap.containsKey(attrName)) throw new SQLObjectAlreadyExistsException("Attribute name already in use.");
        if (provisionalAttributeMap.values().stream().anyMatch(a -> a.attrPosition == attrPosition))
            throw new IllegalArgumentException("Position of attribute already in use.");
        provisionalAttributeMap.put(attrName, new ProvisionalAttribute(attrName, attrPosition, type, isNullable));
    }

    public void setAttributeNullable(String attrName, boolean isNullable) {
        ProvisionalAttribute attr = provisionalAttributeMap.get(attrName);
        if (attr == null) throw new MissingReferencedObjectException("Attribute with name '"+attrName+"' not defined.");
        attr.isNullable = isNullable;
    }

    public void setAttributeType(String attrName, SQLDataType type) {
        ProvisionalAttribute attr = provisionalAttributeMap.get(attrName);
        if (attr == null) throw new MissingReferencedObjectException("Attribute with name '"+attrName+"' not defined.");
        attr.type = type;
    }

    /* - For constraints */

    public void addCheckConstraint(Check checkConstraint) {
        checks.add(checkConstraint);
    }

    public void addDefaultConstraint(String constraintName, String attrName, ValueExpression value) {
        if (constraintName == null || attrName == null || value  == null)
            throw new MissingReferencedObjectException("Some constructor parameter was null (DEFAULT)");
        provisionalDefaultMap.put(constraintName, new ProvisionalDefault(constraintName, attrName, value));
    }

    public void addUniqueConstraint(String constraintName, String attrName) {
        if (constraintName == null || attrName == null)
            throw new MissingReferencedObjectException("Some constructor parameter was null (UNIQUE)");
        ProvisionalUnique pu = provisionalUniqueMap.get(constraintName);
        if (pu == null) provisionalUniqueMap.put(constraintName, new ProvisionalUnique(constraintName, attrName));
        else pu.uniqueAttributes.add(attrName);
    }
    public void addUniqueConstraint(String constraintName, List<String> attrNames) {
        if (constraintName == null || attrNames == null || attrNames.isEmpty())
            throw new MissingReferencedObjectException("Some constructor parameter was null or empty (UNIQUE)");
        ProvisionalUnique pu = provisionalUniqueMap.get(constraintName);
        if (pu == null) provisionalUniqueMap.put(constraintName, new ProvisionalUnique(constraintName, attrNames));
        else pu.uniqueAttributes.addAll(attrNames);
    }

    public void addPrimaryKeyConstraint(String constraintName, String attrName) {
        if (constraintName == null || attrName == null)
            throw new IllegalArgumentException("Some constructor parameter was null (PRIMARY KEY)");
        ProvisionalPrimaryKey pk = provisionalPrimaryKeyMap.get(constraintName);
        if (pk == null) provisionalPrimaryKeyMap.put(constraintName, new ProvisionalPrimaryKey(constraintName, attrName));
        else pk.pkAttributes.add(attrName);
    }
    public void addPrimaryKeyConstraint(String constraintName, List<String> attrNames) {
        if (constraintName == null || attrNames == null || attrNames.isEmpty())
            throw new IllegalArgumentException("Some constructor parameter was null or empty (PRIMARY KEY)");
        ProvisionalPrimaryKey pk = provisionalPrimaryKeyMap.get(constraintName);
        if (pk == null) provisionalPrimaryKeyMap.put(constraintName, new ProvisionalPrimaryKey(constraintName, attrNames));
        else pk.pkAttributes.addAll(attrNames);
    }

    public void addForeignKeyConstraint(String constraintName, String attrName, String referencedTable, String referencedKey) {
        if (attrName == null || referencedKey == null)
            throw new IllegalArgumentException("Some constructor parameter was null (FOREIGN KEY)");
        ProvisionalForeignKey fk = provisionalForeignKeyMap.get(constraintName);
        if (fk == null) provisionalForeignKeyMap.put(constraintName, new ProvisionalForeignKey(constraintName, attrName, referencedTable, referencedKey));
        else {
            fk.fkAttributes.add(attrName);
            fk.referencedAttr.add(referencedKey);
        }
    }
    public void addForeignKeyConstraint(String constraintName, List<String> attrNames, String referencedTable, List<String> referencedKeys) {
        if (constraintName == null || attrNames == null || referencedKeys == null)
            throw new IllegalArgumentException("Some constructor parameter was null (FOREIGN KEY)");
        if (attrNames.size() != referencedKeys.size() || attrNames.isEmpty())
            throw new IllegalArgumentException("Must have the same number of attr. and refs. (not empty)");
        ProvisionalForeignKey fk = provisionalForeignKeyMap.get(constraintName);
        if (fk == null) provisionalForeignKeyMap.put(constraintName, new ProvisionalForeignKey(constraintName, attrNames, referencedTable, referencedKeys));
        else {
            fk.fkAttributes.addAll(attrNames);
            fk.referencedAttr.addAll(referencedKeys);
        }
    }
}
