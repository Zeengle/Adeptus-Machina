import java.util.List;
 
public class Main {
  public static void main(String[] args) {
 
 
    String codigoFonte =
        "<<\"Este é um comentário\">>" +
        "logicum contador;" +
        "per (contador = 1; contador <= 10; contador++){ " +
        "scribere(contador);" +
        "}";
 
 
    try {
      List<Token> tokens = Scanner.lex(codigoFonte);
      Parser parser = new Parser(tokens);
      parser.main();
    } catch (RuntimeException e) {
      System.err.println(e.getMessage());
    }
  }
}