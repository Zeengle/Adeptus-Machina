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

    // mapa tipo-pascal -> lista de nomes de variáveis
    private Map<String, List<String>> variaveis = new LinkedHashMap<>();

    public Translator(List<Token> tokens) {
        this.tokens = tokens;
    }

    // -----------------------------------------------------------------------
    // Helpers de navegação
    // -----------------------------------------------------------------------
    private Token peek()            { return pos < tokens.size() ? tokens.get(pos) : null; }
    private Token peek(int offset)  { int idx = pos + offset; return idx < tokens.size() ? tokens.get(idx) : null; }
    private Token next()            { return pos < tokens.size() ? tokens.get(pos++) : null; }

    private boolean match(String lexema) {
        if (peek() != null && peek().lexema.equals(lexema)) { next(); return true; }
        return false;
    }

    // -----------------------------------------------------------------------
    // Mapeamento de tipos
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Pré-coleta de declarações de variáveis para a secção var do Pascal
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Ponto de entrada da tradução
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // Dispatcher de instruções
    // -----------------------------------------------------------------------
    private void instrucao() {
        Token t = peek();
        if (t == null) return;

        // pula comentários << ... >>
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
        } else if (t.lexema.equals("rumpere")) {
            traduzRumpere();
        } else if (t.lexema.equals("continuare")) {
            traduzContinuare();
        } else if (t.tipo.equals("id")) {
            traduzAtribuicao();
        } else {
            next(); // descarta token desconhecido
        }
    }

    // declarações: apenas consome (variáveis já foram para a seção var)
    private void traduzDeclara() {
        while (peek() != null && !peek().lexema.equals(";")) next();
        match(";");
    }

    // -----------------------------------------------------------------------
    // scribere -> writeln(...)
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // inputus -> readln(...)
    // -----------------------------------------------------------------------
    private void traduzInput() {
        match("inputus");
        match("(");

        output.append("readln(");

        // conteúdo: ID ou string
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

    // -----------------------------------------------------------------------
    // si/alitersi/nisi -> if/else if/else
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // quantum -> while
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // facere (do-while) -> repeat ... until (not condicao)
    // Pascal usa repeat/until com a condição de PARADA (negada em relação ao while)
    // Para simplicidade e fidelidade semântica, usamos a estratégia:
    //   repeat ... until NOT (condicao)
    // -----------------------------------------------------------------------
    private void traduzDoWhile() {
        match("facere");
        match("{");
        output.append("repeat\n");
        while (peek() != null && !peek().lexema.equals("}")) instrucao();
        match("}");
        match("quantum");
        match("(");

        String cond = traduzCondicaoLogica();
        match(")");
        match(";");

        output.append("until not (").append(cond.trim()).append(");\n");
    }

    // -----------------------------------------------------------------------
    // per -> for
    // Suporta expressões complexas no limite (ex: contador <= x + 1)
    // Estrategia: traduz como while para máxima compatibilidade quando o
    // limite não for um literal/ID simples; para casos simples usa for..to/downto
    // -----------------------------------------------------------------------
    private void traduzFor() {
        match("per");
        match("(");

        // init: ID = expr
        String varName = next().lexema;  // ID
        match("=");
        String initExpr = traduzExpr();
        match(";");

        // condição completa como tokens até o próximo ;
        List<Token> condTokens = new ArrayList<>();
        while (peek() != null && !peek().lexema.equals(";")) {
            condTokens.add(next());
        }
        match(";");

        // incremento: ID ++ ou --
        String incVar  = next().lexema;
        boolean decrementa = false;
        if (peek() != null && peek().lexema.equals("--")) { decrementa = true; next(); }
        else if (peek() != null && peek().lexema.equals("++")) { next(); }

        match(")");
        match("{");

        // Tenta traduzir como for..to/downto somente quando a condição é simples:
        // ID <= LIMITE  ou  ID >= LIMITE  (LIMITE = literal ou ID)
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
            // Condição complexa: traduz como while equivalente
            // Inicialização antes do loop
            output.append(varName).append(" := ").append(initExpr).append(";\n");

            // monta string da condição
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

    // -----------------------------------------------------------------------
    // experiri/capere -> try/except (Pascal)
    // -----------------------------------------------------------------------
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

    // -----------------------------------------------------------------------
    // rumpere (break) e continuare (continue)
    // Free Pascal suporta break/continue nativamente dentro de laços
    // -----------------------------------------------------------------------
    private void traduzRumpere() {
        match("rumpere");
        match(";");
        output.append("break;\n");
    }

    private void traduzContinuare() {
        match("continuare");
        match(";");
        output.append("continue;\n");
    }

    // -----------------------------------------------------------------------
    // Atribuição
    // -----------------------------------------------------------------------
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
            // descarta
            while (peek() != null && !peek().lexema.equals(";")) next();
        }
        match(";");
    }

    // -----------------------------------------------------------------------
    // Tradução de expressões como string (para usar dentro de atribuições, etc.)
    // Percorre os tokens que fazem parte da expressão e os devolve como string.
    // -----------------------------------------------------------------------
    private String traduzExpr() {
        StringBuilder sb = new StringBuilder();
        // Conjunto de tokens que NÃO fazem parte de uma expressão (delimitadores)
        while (peek() != null && naoFazParteDeExpr(peek())) {
            Token t = next();
            if (t.lexema.equals("(")) {
                sb.append("(").append(traduzExpr()).append(")");
                match(")");
            } else {
                sb.append(t.lexema).append(" ");
            }
        }
        // Caso acima não tenha pegado nada (primeiro token não era "(")
        if (sb.length() == 0) {
            sb.append(traduzExprCompleta());
        }
        return sb.toString().trim();
    }

    // Traduz uma expressão completa (termo [(+|-) termo]*)
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
            // em Pascal, div inteiro é 'div', mod é 'mod'
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

        // subexpressão entre parênteses
        if (t.lexema.equals("(")) {
            next(); // consome (
            String inner = traduzExprCompleta();
            match(")");
            return "(" + inner + ")";
        }

        // negação unária
        if (t.lexema.equals("-")) {
            next();
            return "-" + traduzFator();
        }

        // literal ou identificador
        if (t.tipo.equals("LITTOTUM")   || t.tipo.equals("LITFRACTUM") ||
            t.tipo.equals("LITLOGICUM") || t.tipo.equals("LITCHAR")    ||
            t.tipo.equals("LITFILUM")   || t.tipo.equals("id")) {
            next();
            // booleanos em Pascal
            if (t.lexema.equals("VERUM"))  return "True";
            if (t.lexema.equals("FALSUM")) return "False";
            // strings: converte aspas duplas para simples (Pascal usa aspas simples)
            if (t.tipo.equals("LITFILUM")) {
                String s = t.lexema.substring(1, t.lexema.length() - 1); // remove " "
                s = s.replace("'", "''"); // escapa aspas simples internas
                return "'" + s + "'";
            }
            // char: o lexema já tem o formato 'x', mantém
            return t.lexema;
        }

        return "";
    }

    private boolean naoFazParteDeExpr(Token t) {
        // Para ser seguro, usa o método correto: retorna FALSE aqui
        // e deixa traduzExprCompleta lidar com tudo
        return false;
    }

    // -----------------------------------------------------------------------
    // Tradução de condições (inside if/while/for)
    // O "(" JÁ FOI consumido pelo chamador. Traduzimos o interior até o ")"
    // de fechamento (que NÃO é consumido aqui — o chamador faz match(")")).
    // -----------------------------------------------------------------------
    private void traduzCondicao() {
        StringBuilder sb = new StringBuilder();
        sb.append(traduzCondicaoLogica());
        output.append(sb.toString().trim());
    }

    // condicao_logica -> condicao_relacional ( (and|or) condicao_relacional )*
    private String traduzCondicaoLogica() {
        StringBuilder sb = new StringBuilder();
        sb.append(traduzCondicaoRelacional());
        while (peek() != null &&
               (peek().lexema.equals("&&") || peek().lexema.equals("||"))) {
            String op = next().lexema.equals("&&") ? " and " : " or ";
            sb.append(op);
            sb.append(traduzCondicaoRelacional());
        }
        return sb.toString();
    }

    // condicao_relacional -> expr op_rel expr  |  expr  (para "VERUM"/"FALSUM" direto)
    private String traduzCondicaoRelacional() {
        String esq = traduzExprCompleta();
        if (peek() != null) {
            String op = peek().lexema;
            String opPasc = null;
            switch (op) {
                case "<":  opPasc = "<";  break;
                case ">":  opPasc = ">";  break;
                case "<=": opPasc = "<="; break;
                case ">=": opPasc = ">="; break;
                case "==": opPasc = "=";  break;
                case "!=": opPasc = "<>"; break;
            }
            if (opPasc != null) {
                next(); // consome operador relacional
                String dir = traduzExprCompleta();
                return esq + " " + opPasc + " " + dir;
            }
        }
        return esq; // ex: condição booleana direta "VERUM"
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

    // -----------------------------------------------------------------------
    // Salvar arquivo
    // -----------------------------------------------------------------------
    public void salvar(String codigo, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            writer.write(codigo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
