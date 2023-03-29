package edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.builders;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Attribute;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.Table;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.Check;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.constraints.ForeignKey;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.MissingReferencedObjectException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.exceptions.SQLObjectAlreadyExistsException;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.sql_data_types.SQLDataType;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.value_expressions.ValueExpression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableSetBuilder {

    /* TABLES INFO */

    /**
     * TableID -> TableBuilder
     */
    private final Map<String, TableBuilder> tables;
    /**
     * TableID -> (FKConstraintName -> ProvisionalForeignKey)
     */
    private final Map<String, Map<String, ProvisionalForeignKey>> tableReferences;

    /* PROVISIONAL FK REFERENCE */

    private static class ProvisionalForeignKey {
        private final String constraintName;
        private final List<String> fkAttributes;
        private final String referencedTable;
        private final SchemaReference referencedSchemaReference;
        private final List<String> referencedAttr;

        public ProvisionalForeignKey(String constraintName, String attr, SchemaReference referencedSchemaReference, String refTable, String refAttr) {
            this.constraintName = constraintName;
            this.fkAttributes = new ArrayList<>();
            this.fkAttributes.add(attr);
            this.referencedTable = refTable;
            this.referencedSchemaReference = referencedSchemaReference;
            this.referencedAttr = new ArrayList<>();
            this.referencedAttr.add(refAttr);
        }
        public ProvisionalForeignKey(String constraintName, List<String> attr, SchemaReference referencedSchemaReference, String refTable, List<String> refAttr) {
            this.constraintName = constraintName;
            this.fkAttributes = new ArrayList<>();
            this.fkAttributes.addAll(attr);
            this.referencedTable = refTable;
            this.referencedSchemaReference = referencedSchemaReference;
            this.referencedAttr = new ArrayList<>();
            this.referencedAttr.addAll(refAttr);
        }

        public ForeignKey getForeignKeyObject(List<Attribute> attributes, List<Table> referencableTables) {
            Table refTable = referencableTables.stream()
                .filter(t -> t.hasSameIdentifier(referencedTable, referencedSchemaReference))
                .findFirst().orElseThrow(() -> new MissingReferencedObjectException("Foreign Key reference not found!"));

            List<Attribute> refAttributes = new ArrayList<>();
            for (String ref : this.referencedAttr) {
                refAttributes.add(refTable.getAttributes()
                    .stream().filter(a -> a.getName().equals(ref))
                    .findFirst().orElseThrow(() -> new MissingReferencedObjectException("Foreign Key reference not found!")));
            }
            return new ForeignKey(
                constraintName,
                fkAttributes.stream().map(fk->attributes.stream().filter(a->a.getName().equals(fk)).findFirst().orElseThrow()).toList(),
                refTable,
                refAttributes
            );
        }
    }

    public TableSetBuilder() {
        this.tables = new HashMap<>();
        this.tableReferences = new HashMap<>();
    }

    /**
     * This method resets the builder's information. If called twice, the second call won't do anything.
     */
    public List<Table> build() {
        Map<String, TableBuilder.MutableTable> processingTables = new HashMap<>();
        for (Map.Entry<String, TableBuilder> entry : tables.entrySet()) {
            processingTables.put(entry.getKey(), entry.getValue().build());
        }
        for (Map.Entry<String, Map<String, ProvisionalForeignKey>> entry : tableReferences.entrySet()) {
            TableBuilder.MutableTable fkTable = processingTables.get(entry.getKey());
            entry.getValue().forEach((key, value) -> fkTable.addFkConstraint(value.getForeignKeyObject(
                fkTable.getAttributes(),
                processingTables.values().stream().map(t -> (Table) t).toList()
            )));
        }
        return processingTables.values().stream().map(t -> (Table)t).toList();
    }

    private TableBuilder getTableBuilder(SchemaReference schema, String tableName) {
        String tableId = getTableId(schema, tableName);

        TableBuilder t = tables.get(tableId);
        if (t == null) {
            t = new TableBuilder(tableName, schema);
            tables.put(tableId, t);
            tableReferences.put(tableId, new HashMap<>());
        }
        return t;
    }

    private Map<String, ProvisionalForeignKey> getTableReferences(SchemaReference schema, String tableName) {
        String tableId = getTableId(schema, tableName);

        Map<String, ProvisionalForeignKey> m = tableReferences.get(tableId);
        if (m == null) {
            m = new HashMap<>();
            tables.put(tableId, new TableBuilder(tableName, schema));
            tableReferences.put(tableId, m);
        }
        return m;
    }

    private String getTableId(SchemaReference schema, String tableName) {
        if (tableName == null) throw new IllegalArgumentException("TableName can't be null!");
        String schemaName = schema != null ? schema.getSchemaName() : "";
        return schemaName != null ? schemaName + "." + tableName : tableName;
    }

    public void addTable(SchemaReference schema, String tableName) {
        String tableId = getTableId(schema, tableName);

        TableBuilder t = tables.get(tableId);
        if (t != null) throw new SQLObjectAlreadyExistsException("Table already exists!");

        t = new TableBuilder(tableName, schema);
        tables.put(tableId, t);
        tableReferences.put(tableId, new HashMap<>());
    }

    /* --------------------------------------------------------------------------------------------------------------- */

    /* ATTRIBUTES SETTERS */

    public void addAttribute(SchemaReference schemaReference, String tableName, String attrName) {
        getTableBuilder(schemaReference, tableName).addAttribute(attrName, null, true, null);
    }

    public void addAttribute(SchemaReference schemaReference, String tableName, String attrName, SQLDataType type) {
        getTableBuilder(schemaReference, tableName).addAttribute(attrName,type, true, null);
    }

    public void addAttribute(SchemaReference schemaReference, String tableName,
                             String attrName, SQLDataType type, boolean isNullable, ValueExpression defaultExpression) {
        getTableBuilder(schemaReference, tableName).addAttribute(attrName,type, isNullable, defaultExpression);
    }

    public void setAttributeNullable(SchemaReference schemaReference, String tableName,
                                     String attrName, boolean isNullable) {
        getTableBuilder(schemaReference, tableName).setAttributeNullable(attrName, isNullable);
    }

    public void setAttributeType(SchemaReference schemaReference, String tableName, String attrName, SQLDataType type) {
        getTableBuilder(schemaReference, tableName).setAttributeType(attrName, type);
    }

    public void setAttributeDefaultExpression(SchemaReference schemaReference, String tableName,
                                              String attrName, ValueExpression defaultExpression) {
        getTableBuilder(schemaReference, tableName).setAttributeDefaultExpression(attrName, defaultExpression);
    }

    /* --------------------------------------------------------------------------------------------------------------- */

    /* CONSTRAINT SETTERS */

    public void addCheckConstraint(SchemaReference schemaReference, String tableName, Check checkConstraint) {
        getTableBuilder(schemaReference, tableName).addCheckConstraint(checkConstraint);
    }


    public void addUniqueConstraint(SchemaReference schemaReference, String tableName, String constraintName, String attrName) {
        getTableBuilder(schemaReference, tableName).addUniqueConstraint(constraintName, attrName);
    }
    public void addUniqueConstraint(SchemaReference schemaReference, String tableName, String constraintName, List<String> attrNames) {
        getTableBuilder(schemaReference, tableName).addUniqueConstraint(constraintName, attrNames);
    }


    public void addPrimaryKeyConstraint(SchemaReference schemaReference, String tableName, String constraintName, String attrName) {
        getTableBuilder(schemaReference, tableName).addPrimaryKeyConstraint(constraintName, attrName);
    }
    public void addPrimaryKeyConstraint(SchemaReference schemaReference, String tableName, String constraintName, List<String> attrNames) {
        getTableBuilder(schemaReference, tableName).addPrimaryKeyConstraint(constraintName, attrNames);
    }

    public void addForeignKeyConstraint(SchemaReference schema, String tableName, String constraintName, String attrName, SchemaReference referencedSchema, String referencedTable, String referencedKey) {
        if (attrName == null || referencedTable == null || referencedKey == null)
            throw new IllegalArgumentException("Some constructor parameter was null (FOREIGN KEY)");
        Map<String, ProvisionalForeignKey> tableFKs = getTableReferences(schema, tableName);
        ProvisionalForeignKey fk = tableFKs.get(constraintName);
        if (fk == null) tableFKs.put(constraintName, new ProvisionalForeignKey(constraintName, attrName, referencedSchema, referencedTable, referencedKey));
        else {
            fk.fkAttributes.add(attrName);
            fk.referencedAttr.add(referencedKey);
        }
    }
    public void addForeignKeyConstraint(SchemaReference schema, String tableName, String constraintName, List<String> attrNames, SchemaReference referencedSchema, String referencedTable, List<String> referencedKeys) {
        if (constraintName == null || attrNames == null || referencedKeys == null)
            throw new IllegalArgumentException("Some constructor parameter was null (FOREIGN KEY)");
        if (attrNames.size() != referencedKeys.size() || attrNames.isEmpty())
            throw new IllegalArgumentException("Must have the same number of attr. and refs. (not empty)");
        Map<String, ProvisionalForeignKey> tableFKs = getTableReferences(schema, tableName);
        ProvisionalForeignKey fk = tableFKs.get(constraintName);
        if (fk == null) tableFKs.put(constraintName, new ProvisionalForeignKey(constraintName, attrNames, referencedSchema, referencedTable, referencedKeys));
        else {
            fk.fkAttributes.addAll(attrNames);
            fk.referencedAttr.addAll(referencedKeys);
        }
    }
}
