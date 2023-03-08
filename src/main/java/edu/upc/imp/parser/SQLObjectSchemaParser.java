package edu.upc.imp.parser;

import edu.upc.imp.parser.sql_server.TSqlLexer;
import edu.upc.imp.parser.sql_server.TSqlParser;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;

public class SQLObjectSchemaParser {
    private final SQLObjectSchema schema;

    public SQLObjectSchemaParser() {
        this.schema = new SQLObjectSchema();
    }

    //TODO: implement this
    /*public SQLObjectSchemaParser(SQLObjectSchema schema) {
        this.schema = new SQLObjectSchema(schema);
    }*/

    public void parse(String sqlStatements) {
        if (sqlStatements == null) throw new IllegalArgumentException("Parser input can not be null.");
        CodePointCharStream input = CharStreams.fromString(sqlStatements);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlParser parser = new TSqlParser(tokens);

        TSqlParser.Tsql_fileContext tree = parser.tsql_file();
        SQLObjectSchemaGrammarVisitorImpl visitor = new SQLObjectSchemaGrammarVisitorImpl(schema);
        visitor.visit(tree);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }
}
