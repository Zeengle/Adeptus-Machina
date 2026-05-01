import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class Scanner {
    public static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
 
        StringBuilder patternBuilder = new StringBuilder();
        patternBuilder.append("(?<RESERVADA>totum|fractum|logicum|filum|char|scribere|inputus|si|alitersi|nisi|quantum|per|experiri|assidus)");
        patternBuilder.append("|(?<LITFRACTUM>\\d+\\.\\d+)");
        patternBuilder.append("|(?<LITTOTUM>\\d+)");
        patternBuilder.append("|(?<LITLOGICUM>VERUM|FALSUM)");
        patternBuilder.append("|(?<LITFILUM>\"[^\"]*\")");
        patternBuilder.append("|(?<LITCHAR>'[a-zA-Z0-9 \\t\\n.?_!]')");
        patternBuilder.append("|(?<OPCOMP><<|>>|<=|>=|==|!=|\\*\\*|//|\\+\\+|--|\\+=|-=|\\*=|/=|&&|\\|\\|)");
        patternBuilder.append("|(?<OPSIMPLES>[+*/%=<>(){},;-])");
        patternBuilder.append("|(?<ID>[a-zA-Z_][a-zA-Z0-9_]*)");
        patternBuilder.append("|(?<WHITESPACE>\\s+)");
        patternBuilder.append("|(?<ERROR>.)");
 
        Pattern pattern = Pattern.compile(patternBuilder.toString());
        Matcher matcher = pattern.matcher(input);
 
        while (matcher.find()) {
            if (matcher.group("RESERVADA") != null) {
                tokens.add(new Token("reservada", matcher.group("RESERVADA")));
            } else if (matcher.group("LITFRACTUM") != null) {
                tokens.add(new Token("LITFRACTUM", matcher.group("LITFRACTUM")));
            } else if (matcher.group("LITTOTUM") != null) {
                tokens.add(new Token("LITTOTUM", matcher.group("LITTOTUM")));
            } else if (matcher.group("LITLOGICUM") != null) {
                tokens.add(new Token("LITLOGICUM", matcher.group("LITLOGICUM")));
            } else if (matcher.group("LITFILUM") != null) {
                tokens.add(new Token("LITFILUM", matcher.group("LITFILUM")));
            } else if (matcher.group("LITCHAR") != null) {
                tokens.add(new Token("LITCHAR", matcher.group("LITCHAR")));
            } else if (matcher.group("OPCOMP") != null) {
                String op = matcher.group("OPCOMP");
                String tipo = classificarOperador(op);
                tokens.add(new Token(tipo, op));
            } else if (matcher.group("OPSIMPLES") != null) {
                String op = matcher.group("OPSIMPLES");
                String tipo = classificarOperadorSimples(op);
                tokens.add(new Token(tipo, op));
            } else if (matcher.group("ID") != null) {
                tokens.add(new Token("id", matcher.group("ID")));
            } else if (matcher.group("WHITESPACE") != null) {
                continue;
            } else if (matcher.group("ERROR") != null) {
                throw new RuntimeException("[Erro Léxico] Caractere inválido: '" + matcher.group("ERROR") + "'");
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