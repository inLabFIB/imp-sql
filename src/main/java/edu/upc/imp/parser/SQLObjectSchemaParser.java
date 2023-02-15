package edu.upc.imp.parser;

import edu.upc.imp.parser.SQLServer.TSqlLexer;
import edu.upc.imp.parser.SQLServer.TSqlParser;
import edu.upc.imp.parser.test.DummyParser;
import edu.upc.imp.sqlobjectschema.SQLObjectSchema;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.net.DatagramPacket;


public class SQLObjectSchemaParser {

    private final String sqlStatements;
    private final SQLObjectSchema schema = new SQLObjectSchema();

    public SQLObjectSchemaParser(String sqlStatements) {
        if (sqlStatements == null) throw new IllegalArgumentException("Parser input can not be null.");
        this.sqlStatements = sqlStatements;
    }

    public void parse() {
        CodePointCharStream input = CharStreams.fromString(sqlStatements);
        TSqlLexer lexer = new TSqlLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TSqlParser parser = new TSqlParser(tokens);

//        TsqlfileContext tree = parser.tsqlfile();
//        SQLObjectSchemaGrammarVisitorImpl visitor = new SQLObjectSchemaGrammarVisitorImpl(schema);
//        visitor.visit(tree);
    }

    public SQLObjectSchema getSQLObjectSchema() {
        return schema;
    }
}
