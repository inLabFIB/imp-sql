package edu.upc.imp.parser;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class SQLObjectSchemaParser {

    private final String sqlStatements;

    public SQLObjectSchemaParser(String sqlStatements) {
        if (sqlStatements == null) throw new IllegalArgumentException("Parser input can not be null.");
        this.sqlStatements = sqlStatements;
    }

    public void parse() {
//        CodePointCharStream input = CharStreams.fromString(sqlStatements);
//        TSqlLexer lexer = new TSqlLexer(input);
//        CommonTokenStream tokens = new CommonTokenStream(lexer);
//        TSqlParser parser = new TSqlParser(tokens);
//        ParseTree tree = parser.tsql_file(); // parse; start at tsql_file
//        System.out.println(tree.toStringTree(parser)); // print tree as text
//        SQLObjectSchemaGrammarVisitorImpl visitor = new SQLObjectSchemaGrammarVisitorImpl();
//        visitor.visit(tree);
    }

}
