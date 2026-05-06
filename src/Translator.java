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

    private Token peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        return null;
    }

    private Token next() {
        if (pos < tokens.size()) return tokens.get(pos++);
        return null;
    }

    private boolean match(String lexema) {
        if (peek() != null && peek().lexema.equals(lexema)) {
            next();
            return true;
        }
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

    private void coletarVariaveis() {
        int i = 0;
        while (i < tokens.size()) {
            Token t = tokens.get(i);

            int offset = 0;
            if (t.lexema.equals("assidus")) {
                offset = 1;
                t = tokens.get(i + offset);
            }

            if (t.tipo.equals("reservada") && ehTipo(t.lexema)) {
                Token proximo = tokens.get(i + offset + 1);
                if (proximo.tipo.equals("id")) {
                    String tipoPasc = tipoPascal(t.lexema);
                    variaveis.computeIfAbsent(tipoPasc, k -> new ArrayList<>()).add(proximo.lexema);
                }
            }
            i++;
        }
    }

    private boolean ehTipo(String lexema) {
        return lexema.equals("totum")   ||
               lexema.equals("fractum") ||
               lexema.equals("logicum") ||
               lexema.equals("filum")   ||
               lexema.equals("char");
    }

    public String traduzir() {
        coletarVariaveis();

        output.append("program Adeptus;\n");

        if (!variaveis.isEmpty()) {
            output.append("var\n");
            for (Map.Entry<String, List<String>> entry : variaveis.entrySet()) {
                String tipo = entry.getKey();
                String nomes = String.join(", ", entry.getValue());
                output.append("    ").append(nomes).append(" : ").append(tipo).append(";\n");
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

        if (t.lexema.equals("assidus")) {
            traduzDeclara();
        } else if (ehTipo(t.lexema)) {
            traduzDeclara();
        } else if (t.lexema.equals("scribere")) {
            traduzScribere();
        } else if (t.lexema.equals("si")) {
            traduzIf();
        } else if (t.lexema.equals("quantum")) {
            traduzWhile();
        } else if (t.lexema.equals("per")) {
            traduzFor();
        } else if (t.tipo.equals("id")) {
            traduzAtribuicao();
        } else {
            next();
        }
    }

    private void traduzDeclara() {
        while (peek() != null && !peek().lexema.equals(";")) {
            next();
        }
        match(";");
    }

    private void traduzScribere() {
        match("scribere");
        match("(");

        output.append("writeln(");

        boolean primeiro = true;
        while (peek() != null && !peek().lexema.equals(")")) {
            if (!primeiro) output.append(", ");
            primeiro = false;

            if (peek().lexema.equals(",")) {
                match(",");
                primeiro = false;
                output.append(", ");
                primeiro = true;
                continue;
            }

            Token t = next();
            output.append(t.lexema);
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
        while (peek() != null && !peek().lexema.equals("}")) {
            instrucao();
        }
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
            while (peek() != null && !peek().lexema.equals("}")) {
                instrucao();
            }
            match("}");
            output.append("end");
        }

        if (peek() != null && peek().lexema.equals("nisi")) {
            match("nisi");
            match("{");
            output.append("\nelse\nbegin\n");
            while (peek() != null && !peek().lexema.equals("}")) {
                instrucao();
            }
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
        while (peek() != null && !peek().lexema.equals("}")) {
            instrucao();
        }
        match("}");
        output.append("end;\n");
    }

    private void traduzFor() {
        match("per");
        match("(");

        Token var   = next();
        match("=");
        Token start = next();
        match(";");

        Token condVar = next();
        String op     = next().lexema;
        Token end     = next();
        match(";");

        Token incVar = next();
        boolean decrementa = false;
        if (peek() != null && peek().lexema.equals("--")) {
            decrementa = true;
        }
        while (peek() != null && !peek().lexema.equals(")")) next();
        match(")");
        match("{");

        if (decrementa) {
            output.append("for ").append(var.lexema)
                  .append(" := ").append(start.lexema)
                  .append(" downto ").append(end.lexema)
                  .append(" do\nbegin\n");
        } else {
            output.append("for ").append(var.lexema)
                  .append(" := ").append(start.lexema)
                  .append(" to ").append(end.lexema)
                  .append(" do\nbegin\n");
        }

        while (peek() != null && !peek().lexema.equals("}")) {
            instrucao();
        }
        match("}");
        output.append("end;\n");
    }

    private void traduzAtribuicao() {
        Token var = next();

        if (peek() != null && peek().lexema.equals("=")) {
            match("=");
            output.append(var.lexema).append(" := ");
            while (peek() != null && !peek().lexema.equals(";")) {
                Token t = next();
                output.append(t.lexema).append(" ");
            }
            match(";");
            output.append(";\n");

        } else if (peek() != null && peek().lexema.equals("++")) {
            match("++");
            match(";");
            output.append(var.lexema).append(" := ").append(var.lexema).append(" + 1;\n");

        } else if (peek() != null && peek().lexema.equals("--")) {
            match("--");
            match(";");
            output.append(var.lexema).append(" := ").append(var.lexema).append(" - 1;\n");

        } else if (peek() != null &&
                   (peek().lexema.equals("+=") || peek().lexema.equals("-=") ||
                    peek().lexema.equals("*=") || peek().lexema.equals("/="))) {
            String opAtrib = next().lexema;
            String opPasc  = opAtrib.replace("=", "");
            output.append(var.lexema).append(" := ").append(var.lexema).append(" ").append(opPasc).append(" ");
            while (peek() != null && !peek().lexema.equals(";")) {
                output.append(next().lexema).append(" ");
            }
            match(";");
            output.append(";\n");

        } else {
            while (peek() != null && !peek().lexema.equals(";")) next();
            match(";");
        }
    }

    private void traduzCondicao() {
        while (peek() != null && !peek().lexema.equals(")")) {
            Token t = next();
            switch (t.lexema) {
                case "&&": output.append("and "); break;
                case "||": output.append("or ");  break;
                case "==": output.append("= ");   break;
                case "!=": output.append("<> ");  break;
                default:   output.append(t.lexema).append(" ");
            }
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