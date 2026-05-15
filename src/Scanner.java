import java.util.ArrayList;
import java.util.List;

public class Scanner {

    private static final String[] PALAVRAS_RESERVADAS = {
        "totum", "fractum", "logicum", "filum", "char",
        "scribere", "inputus",
        "si", "alitersi", "nisi",
        "quantum", "facere",
        "per",
        "experiri", "capere",
        "assidus",
        "rumpere", "continuare", "reddere"
    };

    public static List<Token> lex(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = input.length();

        while (i < len) {
            char c = input.charAt(i);

            // espaços em branco
            if (Character.isWhitespace(c)) { i++; continue; }

            // comentários << ... >>
            if (c == '<' && i + 1 < len && input.charAt(i + 1) == '<') {
                tokens.add(new Token("op_acomentario", "<<"));
                i += 2;
                StringBuilder comentario = new StringBuilder();
                while (i < len) {
                    if (input.charAt(i) == '>' && i + 1 < len && input.charAt(i + 1) == '>') break;
                    comentario.append(input.charAt(i));
                    i++;
                }
                if (i >= len) throw new RuntimeException("[Erro Léxico] Comentário não fechado (faltou '>>') ");
                tokens.add(new Token("LITCOMENTARIO", comentario.toString()));
                tokens.add(new Token("op_fcomentario", ">>"));
                i += 2;
                continue;
            }

            // literal string "..."
            if (c == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                sb.append('"');
                while (i < len && input.charAt(i) != '"') {
                    if (input.charAt(i) == '\n') throw new RuntimeException("[Erro Léxico] String não fechada na linha.");
                    sb.append(input.charAt(i));
                    i++;
                }
                if (i >= len) throw new RuntimeException("[Erro Léxico] String não fechada (faltou '\"').");
                sb.append('"');
                i++;
                tokens.add(new Token("LITFILUM", sb.toString()));
                continue;
            }

            // literal char '.'
            if (c == '\'') {
                i++;
                if (i >= len) throw new RuntimeException("[Erro Léxico] Char literal incompleto.");
                char inner = input.charAt(i);
                i++;
                if (i >= len || input.charAt(i) != '\'')
                    throw new RuntimeException("[Erro Léxico] Char literal não fechado (faltou \"'\").");
                i++;
                tokens.add(new Token("LITCHAR", "'" + inner + "'"));
                continue;
            }

            // números inteiros e decimais
            if (Character.isDigit(c)) {
                StringBuilder sb = new StringBuilder();
                while (i < len && Character.isDigit(input.charAt(i))) { sb.append(input.charAt(i)); i++; }
                if (i < len && input.charAt(i) == '.' && i + 1 < len && Character.isDigit(input.charAt(i + 1))) {
                    sb.append('.');
                    i++;
                    while (i < len && Character.isDigit(input.charAt(i))) { sb.append(input.charAt(i)); i++; }
                    if (i < len && (Character.isLetter(input.charAt(i)) || input.charAt(i) == '_'))
                        throw new RuntimeException("[Erro Léxico] Identificador inválido: '" + sb + input.charAt(i) + "' — identificadores não podem começar com dígito.");
                    tokens.add(new Token("LITFRACTUM", sb.toString()));
                } else {
                    if (i < len && (Character.isLetter(input.charAt(i)) || input.charAt(i) == '_'))
                        throw new RuntimeException("[Erro Léxico] Identificador inválido: '" + sb + input.charAt(i) + "' — identificadores não podem começar com dígito.");
                    tokens.add(new Token("LITTOTUM", sb.toString()));
                }
                continue;
            }

            // identificadores e palavras reservadas
            if (Character.isLetter(c) || c == '_') {
                StringBuilder sb = new StringBuilder();
                while (i < len && (Character.isLetterOrDigit(input.charAt(i)) || input.charAt(i) == '_')) {
                    sb.append(input.charAt(i)); i++;
                }
                String palavra = sb.toString();
                if (palavra.equals("VERUM") || palavra.equals("FALSUM")) {
                    tokens.add(new Token("LITLOGICUM", palavra));
                } else if (isPalavraReservada(palavra)) {
                    tokens.add(new Token("reservada", palavra));
                } else {
                    tokens.add(new Token("id", palavra));
                }
                continue;
            }

            // operadores (compostos primeiro, simples depois)
            Token opToken = lerOperador(input, i);
            if (opToken != null) {
                i += opToken.lexema.length();
                tokens.add(opToken);
                continue;
            }

            throw new RuntimeException("[Erro Léxico] Caractere inválido: '" + c + "'");
        }

        tokens.add(new Token("EOF", "$"));
        return tokens;
    }

    private static boolean isPalavraReservada(String palavra) {
        for (String r : PALAVRAS_RESERVADAS) {
            if (r.equals(palavra)) return true;
        }
        return false;
    }

    private static Token lerOperador(String input, int i) {
        int len = input.length();
        char c  = input.charAt(i);
        char c2 = (i + 1 < len) ? input.charAt(i + 1) : 0;
        String duo = "" + c + c2;

        switch (duo) {
            case "<=": return new Token("op_relacional_composto", "<=");
            case ">=": return new Token("op_relacional_composto", ">=");
            case "==": return new Token("op_relacional_composto", "==");
            case "!=": return new Token("op_relacional_composto", "!=");
            case "++": return new Token("op_incremento", "++");
            case "--": return new Token("op_incremento", "--");
            case "**": return new Token("op_incremento", "**");
            case "//": return new Token("op_incremento", "//");
            case "+=": return new Token("op_atribuicao", "+=");
            case "-=": return new Token("op_atribuicao", "-=");
            case "*=": return new Token("op_atribuicao", "*=");
            case "/=": return new Token("op_atribuicao", "/=");
            case "&&": return new Token("op_logico", "&&");
            case "||": return new Token("op_logico", "||");
        }

        switch (c) {
            case '(': return new Token("op_ap",        "(");
            case ')': return new Token("op_fp",        ")");
            case '{': return new Token("op_ac",        "{");
            case '}': return new Token("op_fc",        "}");
            case ',': return new Token("virgula",       ",");
            case ';': return new Token("fim_linha",     ";");
            case '=': return new Token("op_igualdade",  "=");
            case '<': return new Token("op_relacional", "<");
            case '>': return new Token("op_relacional", ">");
            case '+': return new Token("op_aritmetico", "+");
            case '-': return new Token("op_aritmetico", "-");
            case '*': return new Token("op_aritmetico", "*");
            case '/': return new Token("op_aritmetico", "/");
            case '%': return new Token("op_aritmetico", "%");
        }

        return null;
    }
}