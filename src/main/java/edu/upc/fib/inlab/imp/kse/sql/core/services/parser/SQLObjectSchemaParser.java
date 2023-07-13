package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SchemaReference;
import org.antlr.v4.runtime.*;

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

        SQLLexer lexer = new SQLLexer(input);
        lexer.removeErrorListeners();
        lexer.addErrorListener(new CustomErrorListener());
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        SQLParser parser = new SQLParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new CustomErrorListener());
        SQLParser.Sql_fileContext tree = parser.sql_file();

        SQLObjectSchemaGrammarVisitorImpl visitor = new SQLObjectSchemaGrammarVisitorImpl(schema, defaultSchemaReference);
        visitor.visit(tree);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }

    private static class CustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new RuntimeException("line " + line + ":" + charPositionInLine + " " + msg);
        }

    }
}
