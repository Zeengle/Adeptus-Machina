import java.util.ArrayList;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    List<Token> tokens = new ArrayList<>();
 
    tokens.add(new Token("op_acomentario",   "<<"));
    tokens.add(new Token("FILUM",            "\"Este é um comentário\""));
    tokens.add(new Token("op_fcomentario",  ">>"));
    tokens.add(new Token("reservada",       "per"));
    tokens.add(new Token("op_ap",           "("));
    tokens.add(new Token("id",              "contador"));
    tokens.add(new Token("op_igualdade",    "="));
    tokens.add(new Token("TOTUM",           "1"));
    tokens.add(new Token("fim_linha",       ";"));
    tokens.add(new Token("id",              " contador"));
    tokens.add(new Token("op_relacional",   "<="));
    tokens.add(new Token("TOTUM",           "10"));
    tokens.add(new Token("fim_linha",       ";"));
    tokens.add(new Token("id",              "contador"));
    tokens.add(new Token("op_incremento",   "++"));
    tokens.add(new Token("op_fp",           ")"));
    tokens.add(new Token("op_ac",           "{")); 
    tokens.add(new Token("reservada",        "scribere")); 
    tokens.add(new Token("op_ac",           "("));
    tokens.add(new Token("id",              "contador"));
    tokens.add(new Token("op_fp",           ")"));
    tokens.add(new Token("fim_linha",       ";"));
    tokens.add(new Token("op_fc",           "}"));
 
    Parser parser = new Parser(tokens);
    parser.main();
  }
}
