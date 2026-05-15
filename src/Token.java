import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

public class Token {

  public String tipo;
  public String lexema;
  private static List<Token> tokens = new ArrayList<>();

  public Token(String tipo, String lexema) {
    // Validação de identificadores inválidos
    if (tipo.equals("id")) {
      if (lexema.length() > 0 && Character.isDigit(lexema.charAt(0))) {
        throw new RuntimeException(
          "[Erro Léxico] Identificador inválido: '" + lexema +
          "' — identificadores não podem começar com dígito."
        );
      }
      if (lexema.contains(" ")) {
        throw new RuntimeException(
          "[Erro Léxico] Identificador inválido: '" + lexema +
          "' — identificadores não podem conter espaço."
        );
      }
      if (!lexema.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
        throw new RuntimeException(
          "[Erro Léxico] Identificador inválido: '" + lexema +
          "' — formato não permitido."
        );
      }
    }

    this.lexema = lexema;
    this.tipo = tipo;
    if (!tipo.equals("EOF")) {
      tokens.add(this);
    }
  }

  @Override
  public String toString() {
    return "<" + this.tipo + ", " + this.lexema + "> ";
  }

  public static void salvarTokens(String filename) {
    try (FileWriter writer = new FileWriter(filename)) {
      for (int i = 0; i < tokens.size(); i++) {
        writer.write(tokens.get(i).toString());
      }
      System.out.println("Tokens salvos em: " + filename);
    } catch (IOException e) {
      System.err.println("Erro ao salvar tokens: " + e.getMessage());
    }
  }

  public static void limparTokens() {
    tokens.clear();
  }
}