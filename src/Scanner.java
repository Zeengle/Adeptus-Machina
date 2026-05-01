import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Scanner {
    public static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();

        StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append("(?<RESERVADA>scribere|inputus|si|alitersi|nisi|quantum|per|experiri|assidus)");
        patternBuilder.append("|(?<FRACTUM>\\d+\\.\\d+)");
        patternBuilder.append("|(?<TOTUM>\\d+)");
        patternBuilder.append("|(?<LOGICUM>VERUM|FALSUM)");
        patternBuilder.append("|(?<CHAR>'[a-zA-Z0-9 \\t\\n.?_!]')");
        patternBuilder.append("|(?<FILUM>\"[a-zA-Z0-9 \\t\\n.?_!:]*\")");
        patternBuilder.append("|(?<CHAR>'[a-zA-Z0-9 \\t\\n.?_!]')");
        patternBuilder.append("|(?<OP_COMP><<|>>|<=|>=|==|!=|\\*\\*|//|\\+\\+|--|\\+=|-=|\\*=|/=|&&|\\|\\|)");
        patternBuilder.append("|(?<OP_SIMPLES>[+\\-*/%=<>(){},;])");
        patternBuilder.append("|(?<ID>[a-zA-Z_][a-zA-Z0-9_]*)");
        patternBuilder.append("|(?<WHITESPACE>\\s+)");
        patternBuilder.append("|(?<ERROR>.)"); // Captura qualquer erro

        Pattern pattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group("RESERVADA") != null) {
                tokens.add(new Token("reservada", matcher.group("RESERVADA")));
            } else if (matcher.group("FRACTUM") != null) {
                tokens.add(new Token("FRACTUM", matcher.group("FRACTUM")));
            } else if (matcher.group("TOTUM") != null) {
                tokens.add(new Token("TOTUM", matcher.group("TOTUM")));
            } else if (matcher.group("LOGICUM") != null) {
                tokens.add(new Token("LOGICUM", matcher.group("LOGICUM")));
            } else if (matcher.group("FILUM") != null) {
                tokens.add(new Token("FILUM", matcher.group("FILUM")));
            } else if (matcher.group("CHAR") != null) {
                tokens.add(new Token("CHAR", matcher.group("CHAR")));
            } else if (matcher.group("OP_COMP") != null) {
                String op = matcher.group("OP_COMP");
                String tipo = classificarOperador(op);
                tokens.add(new Token(tipo, op));
            } else if (matcher.group("OP_SIMPLES") != null) {
                String op = matcher.group("OP_SIMPLES");
                String tipo = classificarOperadorSimples(op);
                tokens.add(new Token(tipo, op));
            } else if (matcher.group("ID") != null) {
                tokens.add(new Token("id", matcher.group("ID")));
            } else if (matcher.group("WHITESPACE") != null) {
                continue;
            } else if (matcher.group("ERROR") != null) {
                throw new RuntimeException("Caractere inválido: " + matcher.group("ERROR"));
            }
        }
        
        tokens.add(new Token("EOF", "$"));
        return tokens;
    }

    private static String classificarOperador(String op) {
        switch (op) {
            case "<<":
                return "op_acomentario";
            case ">>":
                return "op_fcomentario";
            case "<=":
            case ">=":
            case "==":
            case "!=":
                return "op_relacional_composto";
            case "++":
            case "--":
            case "**":
            case "//":
                return "op_incremento";
            case "+=":
            case "-=":
            case "*=":
            case "/=":
                return "op_atribuicao";
            case "&&":
            case "||":
                return "op_logico";
            default:
                return "op";
        }
    }

    private static String classificarOperadorSimples(String op) {
        switch (op) {
            case "(":
                return "op_ap";
            case ")":
                return "op_fp";
            case "{":
                return "op_ac";
            case "}":
                return "op_fc";
            case ",":
                return "virgula";
            case ";":
                return "fim_linha";
            case "=":
                return "op_igualdade";
            case "<":
            case ">":
                return "op_relacional";
            case "+":
            case "-":
            case "*":
            case "/":
            case "%":
                return "op_aritmetico";
            default:
                return "op";
        }
    }
}