import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Translator {

    private List<Token> tokens;
    private int pos = 0;
    private StringBuilder output = new StringBuilder();

    private Map<String, List<String>> variaveis = new LinkedHashMap<>();

    public Translator(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek()            { return pos < tokens.size() ? tokens.get(pos) : null; }
    private Token peek(int offset)  { int idx = pos + offset; return idx < tokens.size() ? tokens.get(idx) : null; }
    private Token next()            { return pos < tokens.size() ? tokens.get(pos++) : null; }

    private boolean match(String lexema) {
        if (peek() != null && peek().lexema.equals(lexema)) { next(); return true; }
        return false;
    }

    private String tipoPascal(String tipoAdeptus) {
        switch (tipoAdeptus) {
            case "totum":   return "Integer";
            case "fractum": return "Real";
            case "logicum": return "Boolean";
            case "filum":   return "String";
            case "char":    return "Char";
            default:        return tipoAdeptus;
        }
    }

    private boolean ehTipo(String lexema) {
        return lexema.equals("totum")   || lexema.equals("fractum") ||
               lexema.equals("logicum") || lexema.equals("filum")   ||
               lexema.equals("char");
    }

    private void coletarVariaveis() {
        int i = 0;
        while (i < tokens.size()) {
            Token t = tokens.get(i);
            int offset = 0;

            if (t.lexema.equals("assidus")) { offset = 1; t = tokens.get(i + offset); }

            if (t.tipo.equals("reservada") && ehTipo(t.lexema)) {
                if (i + offset + 1 < tokens.size()) {
                    Token proximo = tokens.get(i + offset + 1);
                    if (proximo.tipo.equals("id")) {
                        String tipoPasc = tipoPascal(t.lexema);
                        variaveis.computeIfAbsent(tipoPasc, k -> new ArrayList<>()).add(proximo.lexema);
                    }
                }
            }
            i++;
        }
    }

    public String traduzir() {
        coletarVariaveis();

        output.append("program Adeptus;\n");

        if (!variaveis.isEmpty()) {
            output.append("var\n");
            for (Map.Entry<String, List<String>> entry : variaveis.entrySet()) {
                String nomes = String.join(", ", entry.getValue());
                output.append("    ").append(nomes).append(" : ").append(entry.getKey()).append(";\n");
            }
        }

        output.append("begin\n");

        while (peek() != null && !peek().tipo.equals("EOF")) {
            instrucao();
        }

        output.append("end.\n");
        return output.toString();
    }

    private void instrucao() {
        Token t = peek();
        if (t == null) return;

        if (t.lexema.equals("<<")) {
            while (peek() != null && !peek().lexema.equals(">>")) next();
            match(">>");
            return;
        }

        if (t.lexema.equals("assidus") || ehTipo(t.lexema)) {
            traduzDeclara();
        } else if (t.lexema.equals("scribere")) {
            traduzScribere();
        } else if (t.lexema.equals("inputus")) {
            traduzInput();
        } else if (t.lexema.equals("si")) {
            traduzIf();
        } else if (t.lexema.equals("quantum")) {
            traduzWhile();
        } else if (t.lexema.equals("facere")) {
            traduzDoWhile();
        } else if (t.lexema.equals("per")) {
            traduzFor();
        } else if (t.lexema.equals("experiri")) {
            traduzExperiri();
        } else if (t.tipo.equals("id")) {
            traduzAtribuicao();
        } else {
            next();
        }
    }

    private void traduzDeclara() {
        while (peek() != null && !peek().lexema.equals(";")) next();
        match(";");
    }

    private void traduzScribere() {
        match("scribere");
        match("(");

        output.append("writeln(");

        boolean primeiro = true;
        while (peek() != null && !peek().lexema.equals(")")) {
            if (peek().lexema.equals(",")) {
                match(",");
                output.append(", ");
                primeiro = true;
                continue;
            }
            if (!primeiro) output.append(", ");
            primeiro = false;
            output.append(traduzExpr());
        }

        match(")");
        output.append(");\n");
        match(";");
    }

    private void traduzInput() {
        match("inputus");
        match("(");

        output.append("readln(");

        if (peek() != null) {
            Token t = next();
            if (t.tipo.equals("LITFILUM")) {
                String s = t.lexema.substring(1, t.lexema.length() - 1);
                s = s.replace("'", "''");
                output.append("'").append(s).append("'");
            } else {
                output.append(t.lexema);
            }
        }

        match(")");
        output.append(");\n");
        match(";");
    }

    private void traduzIf() {
        match("si");
        match("(");
        output.append("if ");
        traduzCondicao();
        match(")");
        match("{");
        output.append(" then\nbegin\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        output.append("end");

        while (peek() != null && peek().lexema.equals("alitersi")) {
            match("alitersi");
            match("(");
            output.append("\nelse if ");
            traduzCondicao();
            match(")");
            match("{");
            output.append(" then\nbegin\n");
            while (peek() != null && !peek().lexema.equals("}")) instrucao();
            match("}");
            output.append("end");
        }

        if (peek() != null && peek().lexema.equals("nisi")) {
            match("nisi");
            match("{");
            output.append("\nelse\nbegin\n");
            while (peek() != null && !peek().lexema.equals("}")) instrucao();
            match("}");
            output.append("end");
        }

        output.append(";\n");
    }

    private void traduzWhile() {
        match("quantum");
        match("(");
        output.append("while ");
        traduzCondicao();
        match(")");
        match("{");
        output.append(" do\nbegin\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        output.append("end;\n");
    }

    private void traduzDoWhile() {
        match("facere");
        match("{");
        output.append("repeat\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        match("quantum");
        match("(");

        StringBuilder cond = new StringBuilder();
        int depth = 1;
        while (peek() != null && depth > 0) {
            if (peek().lexema.equals("("))       depth++;
            else if (peek().lexema.equals(")")) { depth--; if (depth == 0) break; }
            cond.append(traduzTokenCondicao(next())).append(" ");
        }
        match(")");
        match(";");

        output.append("until not (").append(cond.toString().trim()).append(");\n");
    }

    private void traduzFor() {
        match("per");
        match("(");

        String varName = next().lexema;
        match("=");
        String initExpr = traduzExpr();
        match(";");

        List<Token> condTokens = new ArrayList<>();
        while (peek() != null && !peek().lexema.equals(";")) {
            condTokens.add(next());
        }
        match(";");

        String incVar  = next().lexema;
        boolean decrementa = false;
        if (peek() != null && peek().lexema.equals("--")) { decrementa = true; next(); }
        else if (peek() != null && peek().lexema.equals("++")) { next(); }

        match(")");
        match("{");

        if (condTokens.size() == 3 &&
            condTokens.get(0).lexema.equals(varName) &&
            (condTokens.get(1).lexema.equals("<=") || condTokens.get(1).lexema.equals(">="))) {

            String limite  = condTokens.get(2).lexema;
            String direcao = decrementa ? "downto" : "to";

            output.append("for ").append(varName)
                  .append(" := ").append(initExpr)
                  .append(" ").append(direcao).append(" ").append(limite)
                  .append(" do\nbegin\n");

        } else {
            output.append(varName).append(" := ").append(initExpr).append(";\n");

            StringBuilder condStr = new StringBuilder();
            for (Token ct : condTokens) {
                condStr.append(traduzTokenCondicao(ct)).append(" ");
            }

            output.append("while ").append(condStr.toString().trim()).append(" do\nbegin\n");
        }

        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        output.append("end;\n");
    }

    private void traduzExperiri() {
        match("experiri");
        match("{");
        output.append("try\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        match("capere");
        match("(");
        String errVar = peek() != null ? next().lexema : "e";
        match(")");
        match("{");
        output.append("except\n  on E: Exception do begin\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        output.append("  end;\nend;\n");
    }

    private void traduzAtribuicao() {
        Token var = next();

        if (peek() != null && peek().lexema.equals("=")) {
            match("=");
            output.append(var.lexema).append(" := ").append(traduzExpr()).append(";\n");

        } else if (peek() != null && peek().lexema.equals("++")) {
            match("++"); match(";");
            output.append(var.lexema).append(" := ").append(var.lexema).append(" + 1;\n");

        } else if (peek() != null && peek().lexema.equals("--")) {
            match("--"); match(";");
            output.append(var.lexema).append(" := ").append(var.lexema).append(" - 1;\n");

        } else if (peek() != null &&
                   (peek().lexema.equals("+=") || peek().lexema.equals("-=") ||
                    peek().lexema.equals("*=") || peek().lexema.equals("/="))) {
            String opAtrib = next().lexema;
            String opPasc  = opAtrib.replace("=", "");
            String exprStr = traduzExpr();
            output.append(var.lexema).append(" := ").append(var.lexema)
                  .append(" ").append(opPasc).append(" ").append(exprStr).append(";\n");
        } else {

            while (peek() != null && !peek().lexema.equals(";")) next();
        }
        match(";");
    }

    private String traduzExpr() {
        StringBuilder sb = new StringBuilder();

        while (peek() != null && naoFazParteDeExpr(peek())) {
            Token t = next();
            if (t.lexema.equals("(")) {
                sb.append("(").append(traduzExpr()).append(")");
                match(")");
            } else {
                sb.append(t.lexema).append(" ");
            }
        }
        if (sb.length() == 0) {
            sb.append(traduzExprCompleta());
        }
        return sb.toString().trim();
    }

    private String traduzExprCompleta() {
        StringBuilder sb = new StringBuilder();
        sb.append(traduzTermo());
        while (peek() != null && (peek().lexema.equals("+") || peek().lexema.equals("-"))) {
            sb.append(" ").append(next().lexema).append(" ");
            sb.append(traduzTermo());
        }
        return sb.toString();
    }

    private String traduzTermo() {
        StringBuilder sb = new StringBuilder();
        sb.append(traduzFator());
        while (peek() != null && (peek().lexema.equals("*") || peek().lexema.equals("/") ||
               peek().lexema.equals("%") || peek().lexema.equals("//") || peek().lexema.equals("**"))) {
            String op = next().lexema;
            if (op.equals("//"))       sb.append(" div ");
            else if (op.equals("%"))   sb.append(" mod ");
            else                       sb.append(" ").append(op).append(" ");
            sb.append(traduzFator());
        }
        return sb.toString();
    }

    private String traduzFator() {
        if (peek() == null) return "";
        Token t = peek();

        if (t.lexema.equals("(")) {
            next();
            String inner = traduzExprCompleta();
            match(")");
            return "(" + inner + ")";
        }

        if (t.lexema.equals("-")) {
            next();
            return "-" + traduzFator();
        }

        if (t.tipo.equals("LITTOTUM")   || t.tipo.equals("LITFRACTUM") ||
            t.tipo.equals("LITLOGICUM") || t.tipo.equals("LITCHAR")    ||
            t.tipo.equals("LITFILUM")   || t.tipo.equals("id")) {
            next();
            if (t.lexema.equals("VERUM"))  return "True";
            if (t.lexema.equals("FALSUM")) return "False";
            if (t.tipo.equals("LITFILUM")) {
                String s = t.lexema.substring(1, t.lexema.length() - 1);
                s = s.replace("'", "''");
                return "'" + s + "'";
            }
            return t.lexema;
        }

        return "";
    }

    private boolean naoFazParteDeExpr(Token t) {
        return false;
    }

    private void traduzCondicao() {
        int depth = 1;
        StringBuilder sb = new StringBuilder();
        while (peek() != null && depth > 0) {
            if (peek().lexema.equals("("))       { depth++; sb.append("("); next(); continue; }
            if (peek().lexema.equals(")"))       { depth--; if (depth == 0) break; sb.append(")"); next(); continue; }
            sb.append(traduzTokenCondicao(next())).append(" ");
        }
        output.append(sb.toString().trim());
    }

    private String traduzTokenCondicao(Token t) {
        if (t.tipo.equals("LITFILUM")) {
            String s = t.lexema.substring(1, t.lexema.length() - 1);
            s = s.replace("'", "''");
            return "'" + s + "'";
        }
        switch (t.lexema) {
            case "&&":    return "and";
            case "||":    return "or";
            case "==":    return "=";
            case "!=":    return "<>";
            case "VERUM": return "True";
            case "FALSUM":return "False";
            default:      return t.lexema;
        }
    }

    public void salvar(String codigo, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            writer.write(codigo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
