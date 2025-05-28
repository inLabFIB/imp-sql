package edu.upc.fib.inlab.imp.kse.sql.core.services.parser;

import edu.upc.fib.inlab.imp.kse.sql.core.exceptions.IMPSqlException;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SQLObjectSchema;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.SchemaReference;
import edu.upc.fib.inlab.imp.kse.sql.core.schema.relational_expressions.Query;
import org.antlr.v4.runtime.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The parser stores a copy of the schema (possibly) passed as a parameter to the constructor.
 * From this point on, all the schemas returned are the same object, and parse() method calls can modify it.
 */
public class StandardSQLParser {

    private final SQLObjectSchema schema;
    private final List<Query> queries;

    public StandardSQLParser() {
        this.schema = new SQLObjectSchema();
        this.queries = new ArrayList<>();
    }

    public StandardSQLParser(SQLObjectSchema schema) {
        this.schema = schema.getCopy();
        this.queries = new ArrayList<>();
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

        StandardSQLGrammarVisitorImpl visitor = new StandardSQLGrammarVisitorImpl(schema, queries, defaultSchemaReference);
        visitor.visit(tree);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }

    public List<Query> getQueries() {
        return queries;
    }

    private static class CustomErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
            throw new IMPSqlException("line " + line + ":" + charPositionInLine + " " + msg);
        }

    }
}
