package edu.upc.fib.inlab.imp.kse.sql.core.schema;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.visitor.SQLObjectSchemaValueObject;

import java.util.List;

public interface TableSource extends SQLObjectSchemaValueObject {

    String getName();

    SchemaReference getSchemaReference();

    List<String> getColumnNames();
}
