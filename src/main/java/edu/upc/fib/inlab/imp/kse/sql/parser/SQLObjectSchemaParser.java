package edu.upc.fib.inlab.imp.kse.sql.parser;

import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.sqlobjectschema.SchemaReference;
import edu.upc.imp.sql.parser.sql_server.TSqlLexer;
import edu.upc.imp.sql.parser.sql_server.TSqlParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

/**
 * The parser stores a copy of the schema (possibly) passed as a parameter to the constructor.
 * From this point on, all the schemas returned are the same object, and parse() method calls can modify it.
 */
public class SQLObjectSchemaParser {

    private final SQLObjectSchema schema;

    public SQLObjectSchemaParser() {
        this.schema = new SQLObjectSchema();
    }

    public SQLObjectSchemaParser(SQLObjectSchema schema) {
        this.schema = schema.getCopy();
    }

    public void parse(String sqlStatements) {
        parse(sqlStatements, null);
    }
    public void parse(String sqlStatements, SchemaReference defaultSchemaReference) {
        if (sqlStatements == null) throw new IllegalArgumentException("Parser input can not be null.");
        CodePointCharStream input = CharStreams.fromString(sqlStatements);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlParser parser = new TSqlParser(tokens);

        TSqlParser.Tsql_fileContext tree = parser.tsql_file();
        SQLObjectSchemaGrammarVisitorImpl visitor = new SQLObjectSchemaGrammarVisitorImpl(schema, defaultSchemaReference);
        visitor.visit(tree);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }
}
