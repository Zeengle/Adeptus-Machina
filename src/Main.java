import java.util.List;

public class Main {
    public static void main(String[] args) {

        String codigoFonte =
            "totum x = 5;\n" +
            "fractum y;\n" +
            "scribere(x + y);\n";

        try {
            List<Token> tokensParser = Scanner.lex(codigoFonte);

            List<Token> tokensSem = Scanner.lex(codigoFonte);

            Parser parser = new Parser(tokensParser);
            parser.main();

            Token.limparTokens();
            SemanticAnalyzer sem = new SemanticAnalyzer(tokensSem);
            sem.analisar();

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}