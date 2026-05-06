import java.util.*;

public class SemanticAnalyzer {

    public enum Tipo {
        TOTUM,
        FRACTUM,
        LOGICUM,
        FILUM,
        CHAR,
        DESCONHECIDO
    }

    private static class Simbolo {
        String nome;
        Tipo tipo;
        boolean inicializado;
        boolean constante;

        Simbolo(String nome, Tipo tipo, boolean inicializado, boolean constante) {
            this.nome = nome;
            this.tipo = tipo;
            this.inicializado = inicializado;
            this.constante = constante;
        }
    }

    private static class TabelaSimbolos {
        private Deque<Map<String, Simbolo>> pilha = new ArrayDeque<>();

        void entrarEscopo() {
            pilha.push(new LinkedHashMap<>());
        }

        void sairEscopo() {
            if (!pilha.isEmpty()) pilha.pop();
        }

        boolean declarar(String nome, Tipo tipo, boolean inicializado, boolean constante) {
            Map<String, Simbolo> escopoAtual = pilha.peek();
            if (escopoAtual == null) return false;
            if (escopoAtual.containsKey(nome)) return false;
            escopoAtual.put(nome, new Simbolo(nome, tipo, inicializado, constante));
            return true;
        }

        Simbolo buscar(String nome) {
            for (Map<String, Simbolo> escopo : pilha) {
                if (escopo.containsKey(nome)) return escopo.get(nome);
            }
            return null;
        }

        void marcarInicializado(String nome) {
            for (Map<String, Simbolo> escopo : pilha) {
                if (escopo.containsKey(nome)) {
                    escopo.get(nome).inicializado = true;
                    return;
                }
            }
        }
    }

    private final List<Token> tokens;
    private int pos = 0;
    private final TabelaSimbolos tabela = new TabelaSimbolos();
    private final List<String> erros   = new ArrayList<>();
    private final List<String> avisos  = new ArrayList<>();

    public SemanticAnalyzer(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        return null;
    }

    private Token peek(int offset) {
        int idx = pos + offset;
        if (idx < tokens.size()) return tokens.get(idx);
        return null;
    }

    private Token next() {
        if (pos < tokens.size()) return tokens.get(pos++);
        return null;
    }

    private boolean match(String lexema) {
        if (peek() != null && peek().lexema.equals(lexema)) { next(); return true; }
        return false;
    }

    private boolean matchTipo(String tipo) {
        if (peek() != null && peek().tipo.equals(tipo)) { next(); return true; }
        return false;
    }

    private void consumirAte(String lexema) {
        while (peek() != null && !peek().lexema.equals(lexema) && !peek().tipo.equals("EOF"))
            next();
        match(lexema);
    }

    private Tipo tipoDoLexema(String tipoToken) {
        switch (tipoToken) {
            case "LITTOTUM":   return Tipo.TOTUM;
            case "LITFRACTUM": return Tipo.FRACTUM;
            case "LITLOGICUM": return Tipo.LOGICUM;
            case "LITFILUM":   return Tipo.FILUM;
            case "LITCHAR":    return Tipo.CHAR;
            default:           return Tipo.DESCONHECIDO;
        }
    }

    private Tipo tipoReservado(String lexema) {
        switch (lexema) {
            case "totum":   return Tipo.TOTUM;
            case "fractum": return Tipo.FRACTUM;
            case "logicum": return Tipo.LOGICUM;
            case "filum":   return Tipo.FILUM;
            case "char":    return Tipo.CHAR;
            default:        return Tipo.DESCONHECIDO;
        }
    }

    private boolean ehNumerico(Tipo t) {
        return t == Tipo.TOTUM || t == Tipo.FRACTUM;
    }

    private boolean ehTipoCompativel(Tipo destino, Tipo origem) {
        if (destino == origem) return true;
        if (ehNumerico(destino) && ehNumerico(origem)) return true;
        return false;
    }

    private void erro(String msg) {
        erros.add("[ERRO SEMÂNTICO] " + msg);
    }

    private void aviso(String msg) {
        avisos.add("[AVISO] " + msg);
    }

    public boolean analisar() {
        tabela.entrarEscopo();
        analisarBloco();
        tabela.sairEscopo();

        System.out.println("\n--- Análise Semântica ---");

        if (erros.isEmpty() && avisos.isEmpty()) {
            System.out.println("Semanticamente correta — nenhum erro encontrado.");
            return true;
        }

        for (String a : avisos) System.out.println(a);
        for (String e : erros)  System.out.println(e);

        if (erros.isEmpty()) {
            System.out.println("Análise concluída com avisos.");
            return true;
        }

        System.out.println("Análise concluída com " + erros.size() + " erro(s).");
        return false;
    }

    public List<String> getErros()  { return Collections.unmodifiableList(erros); }
    public List<String> getAvisos() { return Collections.unmodifiableList(avisos); }

    private void analisarBloco() {
        while (peek() != null && !peek().lexema.equals("}") && !peek().tipo.equals("EOF")) {
            analisarInstrucao();
        }
    }

    private void analisarInstrucao() {
        Token t = peek();
        if (t == null) return;

        if (t.lexema.equals("<<")) {
            while (peek() != null && !peek().lexema.equals(">>")) next();
            match(">>");
            return;
        }

        if (t.lexema.equals("assidus") || ehTipoStr(t.lexema)) {
            analisarDeclara();
        } else if (t.lexema.equals("scribere")) {
            analisarScribere();
        } else if (t.lexema.equals("inputus")) {
            analisarInput();
        } else if (t.lexema.equals("si")) {
            analisarSi();
        } else if (t.lexema.equals("quantum")) {
            analisarQuantum();
        } else if (t.lexema.equals("per")) {
            analisarPer();
        } else if (t.lexema.equals("experiri")) {
            analisarExperiri();
        } else if (t.tipo.equals("id")) {
            analisarAtribuicao();
        } else {
            next();
        }
    }

    private boolean ehTipoStr(String lexema) {
        return lexema.equals("totum") || lexema.equals("fractum") ||
               lexema.equals("logicum") || lexema.equals("filum") || lexema.equals("char");
    }

    private void analisarDeclara() {
        boolean constante = false;
        if (peek() != null && peek().lexema.equals("assidus")) {
            constante = true;
            next();
        }

        Token tokenTipo = peek();
        if (tokenTipo == null || !ehTipoStr(tokenTipo.lexema)) {
            erro("Tipo inválido ou ausente na declaração.");
            consumirAte(";");
            return;
        }
        Tipo tipo = tipoReservado(tokenTipo.lexema);
        next();

        Token tokenId = peek();
        if (tokenId == null || !tokenId.tipo.equals("id")) {
            erro("Identificador ausente na declaração de variável.");
            consumirAte(";");
            return;
        }
        String nome = tokenId.lexema;
        next(); 

        boolean declaradaOk = tabela.declarar(nome, tipo, false, constante);
        if (!declaradaOk) {
            erro("Variável '" + nome + "' já foi declarada neste escopo.");
        }

        if (peek() != null && peek().lexema.equals("=")) {
            next();
            Tipo tipoExpr = analisarExpr();
            tabela.marcarInicializado(nome);

            if (tipoExpr != Tipo.DESCONHECIDO && !ehTipoCompativel(tipo, tipoExpr)) {
                erro("Tipo incompatível na declaração de '" + nome + "': esperado " +
                     tipo + ", recebido " + tipoExpr + ".");
            }
        } else if (constante) {
            aviso("Constante '" + nome + "' declarada sem valor inicial.");
        }

        match(";");
    }

    private void analisarAtribuicao() {
        Token tokenId = next();
        String nome = tokenId.lexema;

        Simbolo s = tabela.buscar(nome);
        if (s == null) {
            erro("Variável '" + nome + "' usada sem ter sido declarada.");
            consumirAte(";");
            return;
        }
        if (s.constante && s.inicializado) {
            erro("Constante '" + nome + "' não pode ser reatribuída.");
            consumirAte(";");
            return;
        }

        Token op = peek();
        if (op == null) return;

        if (op.lexema.equals("=")) {
            next();
            Tipo tipoExpr = analisarExpr();
            tabela.marcarInicializado(nome);

            if (tipoExpr != Tipo.DESCONHECIDO && !ehTipoCompativel(s.tipo, tipoExpr)) {
                erro("Tipo incompatível ao atribuir a '" + nome + "': esperado " +
                     s.tipo + ", recebido " + tipoExpr + ".");
            }

        } else if (op.lexema.equals("++") || op.lexema.equals("--")) {
            next();
            if (!ehNumerico(s.tipo)) {
                erro("Operador '" + op.lexema + "' aplicado a variável não numérica '" + nome + "' (tipo: " + s.tipo + ").");
            }
            if (!s.inicializado) {
                aviso("Variável '" + nome + "' pode não ter sido inicializada antes do uso.");
            }

        } else if (op.lexema.equals("+=") || op.lexema.equals("-=") ||
                   op.lexema.equals("*=") || op.lexema.equals("/=")) {
            next();
            if (!ehNumerico(s.tipo)) {
                erro("Operador '" + op.lexema + "' aplicado a variável não numérica '" + nome + "'.");
            }
            if (!s.inicializado) {
                aviso("Variável '" + nome + "' pode não ter sido inicializada antes do uso.");
            }
            analisarExpr();
        }

        match(";");
    }

    private void analisarScribere() {
        match("scribere");
        match("(");

        while (peek() != null && !peek().lexema.equals(")") && !peek().tipo.equals("EOF")) {
            if (peek().lexema.equals(",")) { next(); continue; }
            analisarExpr();
        }

        match(")");
        match(";");
    }

    private void analisarInput() {
        match("inputus");
        match("(");

        Token t = peek();
        if (t != null && t.tipo.equals("id")) {
            String nome = t.lexema;
            Simbolo s = tabela.buscar(nome);
            if (s == null) {
                erro("Variável '" + nome + "' em inputus não foi declarada.");
            } else {
                tabela.marcarInicializado(nome);
            }
            next();
        } else if (t != null && t.tipo.equals("LITFILUM")) {
            next();
        }

        match(")");
        match(";");
    }

    private void analisarSi() {
        match("si");
        match("(");
        analisarCondicao();
        match(")");
        match("{");
        tabela.entrarEscopo();
        analisarBloco();
        tabela.sairEscopo();
        match("}");

        while (peek() != null && peek().lexema.equals("alitersi")) {
            next();
            match("(");
            analisarCondicao();
            match(")");
            match("{");
            tabela.entrarEscopo();
            analisarBloco();
            tabela.sairEscopo();
            match("}");
        }

        if (peek() != null && peek().lexema.equals("nisi")) {
            next();
            match("{");
            tabela.entrarEscopo();
            analisarBloco();
            tabela.sairEscopo();
            match("}");
        }
    }

    private void analisarQuantum() {
        match("quantum");
        match("(");
        analisarCondicao();
        match(")");
        match("{");
        tabela.entrarEscopo();
        analisarBloco();
        tabela.sairEscopo();
        match("}");
    }

    private void analisarPer() {
        match("per");
        match("(");

        tabela.entrarEscopo();

        Token tokenId = peek();
        if (tokenId != null && tokenId.tipo.equals("id")) {
            String nome = tokenId.lexema;
            next();

            Simbolo s = tabela.buscar(nome);
            if (s == null) {
                erro("Variável de controle '" + nome + "' do 'per' não foi declarada.");
            } else if (!ehNumerico(s.tipo)) {
                erro("Variável de controle '" + nome + "' do 'per' deve ser numérica (tipo: " + s.tipo + ").");
            }

            if (peek() != null && peek().lexema.equals("=")) {
                next();
                Tipo tipoExpr = analisarExpr();
                tabela.marcarInicializado(nome);
                if (s != null && tipoExpr != Tipo.DESCONHECIDO && !ehTipoCompativel(s.tipo, tipoExpr)) {
                    erro("Tipo incompatível na inicialização de '" + nome + "' no 'per'.");
                }
            }
        }
        match(";");
        analisarCondicao();
        match(";");

        Token tokenInc = peek();
        if (tokenInc != null && tokenInc.tipo.equals("id")) {
            String nome = tokenInc.lexema;
            next();
            Simbolo s = tabela.buscar(nome);
            if (s == null) {
                erro("Variável '" + nome + "' usada no incremento do 'per' não foi declarada.");
            }
            Token opInc = peek();
            if (opInc != null && (opInc.lexema.equals("++") || opInc.lexema.equals("--"))) {
                next();
            }
        }

        match(")");
        match("{");
        analisarBloco();
        tabela.sairEscopo();
        match("}");
    }

    private void analisarExperiri() {
        match("experiri");
        match("{");
        tabela.entrarEscopo();
        analisarBloco();
        tabela.sairEscopo();
        match("}");

        if (peek() != null && peek().lexema.equals("capere")) {
            next();
            match("(");
            Token tokenId = peek();
            if (tokenId != null && tokenId.tipo.equals("id")) {
                String nome = tokenId.lexema;
                next();
                tabela.entrarEscopo();
                tabela.declarar(nome, Tipo.FILUM, true, false);
                match(")");
                match("{");
                analisarBloco();
                tabela.sairEscopo();
                match("}");
            }
        }
    }

    private void analisarCondicao() {
        analisarCondicaoRelacional();

        while (peek() != null &&
               (peek().lexema.equals("&&") || peek().lexema.equals("||"))) {
            next();
            analisarCondicaoRelacional();
        }
    }

    private void analisarCondicaoRelacional() {
        Tipo esq = analisarExpr();

        Token op = peek();
        if (op == null) return;

        String opLex = op.lexema;
        if (opLex.equals("<") || opLex.equals(">") || opLex.equals("<=") ||
            opLex.equals(">=") || opLex.equals("==") || opLex.equals("!=")) {
            next();
            Tipo dir = analisarExpr();

            if (esq != Tipo.DESCONHECIDO && dir != Tipo.DESCONHECIDO) {
                boolean esqNum = ehNumerico(esq);
                boolean dirNum = ehNumerico(dir);

                if (esqNum != dirNum && !(esq == Tipo.DESCONHECIDO || dir == Tipo.DESCONHECIDO)) {
                    erro("Comparação entre tipos incompatíveis: " + esq + " e " + dir + ".");
                }
            }
        }
    }

    private Tipo analisarExpr() {
        Tipo tipo = analisarTermo();

        while (peek() != null &&
               (peek().lexema.equals("+") || peek().lexema.equals("-"))) {
            Token op = next();
            Tipo direito = analisarTermo();

            if (op.lexema.equals("+") && tipo == Tipo.FILUM && direito == Tipo.FILUM) {
                tipo = Tipo.FILUM;
            } else if (!ehNumerico(tipo) || !ehNumerico(direito)) {
                if (tipo != Tipo.DESCONHECIDO && direito != Tipo.DESCONHECIDO)
                    erro("Operação aritmética entre tipos não numéricos: " + tipo + " e " + direito + ".");
            } else {
                tipo = (tipo == Tipo.FRACTUM || direito == Tipo.FRACTUM) ? Tipo.FRACTUM : Tipo.TOTUM;
            }
        }
        return tipo;
    }

    private Tipo analisarTermo() {
        Tipo tipo = analisarFator();

        while (peek() != null &&
               (peek().lexema.equals("*") || peek().lexema.equals("/") ||
                peek().lexema.equals("%") || peek().lexema.equals("//") ||
                peek().lexema.equals("**"))) {
            next();
            Tipo direito = analisarFator();

            if (!ehNumerico(tipo) || !ehNumerico(direito)) {
                if (tipo != Tipo.DESCONHECIDO && direito != Tipo.DESCONHECIDO)
                    erro("Operação aritmética entre tipos não numéricos: " + tipo + " e " + direito + ".");
            } else {
                tipo = (tipo == Tipo.FRACTUM || direito == Tipo.FRACTUM) ? Tipo.FRACTUM : Tipo.TOTUM;
            }
        }
        return tipo;
    }

    private Tipo analisarFator() {
        Token t = peek();
        if (t == null) return Tipo.DESCONHECIDO;

        if (t.tipo.equals("LITTOTUM"))   { next(); return Tipo.TOTUM;    }
        if (t.tipo.equals("LITFRACTUM")) { next(); return Tipo.FRACTUM;  }
        if (t.tipo.equals("LITFILUM"))   { next(); return Tipo.FILUM;    }
        if (t.tipo.equals("LITCHAR"))    { next(); return Tipo.CHAR;     }
        if (t.tipo.equals("LITLOGICUM")) { next(); return Tipo.LOGICUM;  }

        if (t.tipo.equals("id")) {
            String nome = t.lexema;
            next();
            Simbolo s = tabela.buscar(nome);

            if (s == null) {
                erro("Variável '" + nome + "' usada sem ter sido declarada.");
                return Tipo.DESCONHECIDO;
            }
            if (!s.inicializado) {
                aviso("Variável '" + nome + "' pode não ter sido inicializada antes do uso.");
            }
            return s.tipo;
        }

        if (t.lexema.equals("(")) {
            next();
            Tipo tipo = analisarExpr();
            match(")");
            return tipo;
        }
        if (t.lexema.equals("-")) {
            next();
            Tipo tipo = analisarFator();
            if (!ehNumerico(tipo) && tipo != Tipo.DESCONHECIDO) {
                erro("Operador unário '-' aplicado a tipo não numérico: " + tipo + ".");
            }
            return tipo;
        }

        return Tipo.DESCONHECIDO;
    }
}
