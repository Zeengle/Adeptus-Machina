import java.util.List;

public class Main {
    public static void main(String[] args) {

        String codigoFonte =
            "<< Programa de teste do compilador Adeptus Machina >>\n" +
            "totum x = 10;\n" +
            "fractum y = 3.14;\n" +
            "filum nome = \"Adeptus\";\n" +
            "logicum flag = VERUM;\n" +
            "\n" +
            "scribere(\"Iniciando testes\");\n" +
            "\n" +
            "si (x > 5) {\n" +
            "    scribere(\"x maior que 5\");\n" +
            "} alitersi (x == 5) {\n" +
            "    scribere(\"x igual a 5\");\n" +
            "} nisi {\n" +
            "    scribere(\"x menor que 5\");\n" +
            "}\n" +
            "\n" +
            "totum i = 0;\n" +
            "quantum (i < 3) {\n" +
            "    scribere(i);\n" +
            "    i++;\n" +
            "}\n" +
            "\n" +
            "totum k = 0;\n" +
            "facere {\n" +
            "    scribere(k);\n" +
            "    k++;\n" +
            "} quantum (k < 3);\n" +
            "\n" +
            "totum j;\n" +
            "per (j = 1; j <= 5; j++) {\n" +
            "    scribere(j);\n" +
            "}\n";

        try {
            Token.limparTokens();
            List<Token> tokensParser = Scanner.lex(codigoFonte);
            List<Token> tokensSem   = Scanner.lex(codigoFonte);
            List<Token> tokensTrad  = Scanner.lex(codigoFonte);

            // Fase Léxica
            System.out.println("=== FASE LÉXICA ===");
            for (Token t : tokensSem) System.out.print(t + " ");
            System.out.println();

            // Fase Sintática
            System.out.println("\n=== FASE SINTÁTICA ===");
            Token.limparTokens();
            Parser parser = new Parser(tokensParser);
            parser.main();

            // Fase Semântica
            System.out.println("\n=== FASE SEMÂNTICA ===");
            Token.limparTokens();
            SemanticAnalyzer sem = new SemanticAnalyzer(tokensSem);
            sem.analisar();

            // Tradução
            System.out.println("\n=== TRADUÇÃO PARA PASCAL ===");
            Token.limparTokens();
            Translator translator = new Translator(tokensTrad);
            String pascal = translator.traduzir();
            System.out.println(pascal);
            translator.salvar(pascal, "output.pas");
            System.out.println("Arquivo output.pas gerado.");

        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
        }
    }
}
