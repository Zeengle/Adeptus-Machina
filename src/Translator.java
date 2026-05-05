import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class Translator {

    private List<Token> tokens;
    private int pos = 0;
    private StringBuilder output = new StringBuilder();

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

    // =========================
    // ENTRY POINT
    // =========================
    public String traduzir() {
        output.append("program Adeptus;\n");
        output.append("begin\n");

        while (peek() != null && !peek().tipo.equals("EOF")) {
            instrucao();
        }

        output.append("end.\n");
        return output.toString();
    }

    // =========================
    // INSTRUÇÕES
    // =========================
    private void instrucao() {
        Token t = peek();

        if (t.lexema.equals("scribere")) {
            traduzScribere();
        } else if (t.lexema.equals("si")) {
            traduzIf();
        } else if (t.lexema.equals("per")) {
            traduzFor();
        } else if (t.tipo.equals("id")) {
            traduzAtribuicao();
        } else {
            next(); // ignora
        }
    }

    // =========================
    // PRINT
    // =========================
    private void traduzScribere() {
        match("scribere");
        match("(");

        output.append("writeln(");

        while (!match(")")) {
            Token t = next();

            if (t.tipo.equals("LITFILUM")) {
                output.append(t.lexema);
            } else if (t.tipo.equals("id") || t.tipo.startsWith("LIT")) {
                output.append(t.lexema);
            }

            if (peek() != null && peek().lexema.equals(",")) {
                match(",");
                output.append(", ");
            }
        }

        output.append(");\n");
        match(";");
    }

    // =========================
    // IF
    // =========================
    private void traduzIf() {
        match("si");
        match("(");

        output.append("if ");
        traduzCondicao();

        match(")");
        match("{");

        output.append(" then\nbegin\n");

        while (!match("}")) {
            instrucao();
        }

        output.append("end;\n");
    }

    // =========================
    // FOR
    // =========================
    private void traduzFor() {
        match("per");
        match("(");

        // init
        Token var = next(); // id
        match("=");
        Token start = next();

        match(";");

        // condição
        Token condVar = next();
        String op = next().lexema;
        Token end = next();

        match(";");

        // incremento (ignorado parcialmente)
        while (!match(")")) next();

        match("{");

        output.append("for " + var.lexema + " := " + start.lexema +
                " to " + end.lexema + " do\nbegin\n");

        while (!match("}")) {
            instrucao();
        }

        output.append("end;\n");
    }

    // =========================
    // ATRIBUIÇÃO
    // =========================
    private void traduzAtribuicao() {
        Token var = next();

        if (match("=")) {
            output.append(var.lexema + " := ");

            while (!match(";")) {
                Token t = next();
                output.append(t.lexema + " ");
            }

            output.append(";\n");
        }
    }

    // =========================
    // CONDIÇÃO
    // =========================
    private void traduzCondicao() {
        while (peek() != null && !peek().lexema.equals(")")) {
            Token t = next();

            switch (t.lexema) {
                case "&&": output.append("and "); break;
                case "||": output.append("or "); break;
                case "==": output.append("="); break;
                case "!=": output.append("<>"); break;
                default: output.append(t.lexema + " ");
            }
        }
    }

    // =========================
    // SALVAR ARQUIVO
    // =========================
    public void salvar(String codigo, String nomeArquivo) {
        try (FileWriter writer = new FileWriter(nomeArquivo)) {
            writer.write(codigo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}