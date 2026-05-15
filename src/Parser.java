import java.util.List;

public class Parser {

  List<Token> tokens;
  Token token;
  Tree tree;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.tree   = new Tree();
  }

  public void main() {
    token = getNextToken();
    Node raiz = new Node("prog");
    if (prog(raiz)) {
      if (token == null || token.tipo.equals("EOF")) {
        tree.setRoot(raiz);
        System.out.println("Sintaticamente correta");
        System.out.println("\n--- Arvore Sintatica ---");
        tree.printTree();
        Token.salvarTokens("tokens.txt");
        return;
      }
    }
    erro();
  }

  public Token getNextToken() {
    if (tokens.size() > 0) return tokens.remove(0);
    return null;
  }

  private boolean matchL(String palavra, Node pai) {
    if (token != null && token.lexema.equals(palavra)) {
      if (pai != null) pai.addNode(palavra);
      token = getNextToken();
      return true;
    }
    return false;
  }

  private boolean matchT(String tipo, Node pai) {
    if (token != null && token.tipo.equals(tipo)) {
      if (pai != null) pai.addNode(tipo + "(" + token.lexema + ")");
      token = getNextToken();
      return true;
    }
    return false;
  }

  private boolean matchL(String palavra) { return matchL(palavra, null); }
  private boolean matchT(String tipo)    { return matchT(tipo,    null); }

  private void erro() {
    if (token != null)
      System.out.println("Erro sintatico: '" + token.lexema + "' (tipo: " + token.tipo + ")");
    else
      System.out.println("Erro: fim inesperado da entrada");
  }

  private boolean AP(Node pai) {return matchL("(" , pai);   }
  private boolean FP(Node pai) {return matchL(")" , pai);   }
  private boolean AC(Node pai) {return matchL("{" , pai);   }
  private boolean FC(Node pai) {return matchL("}" , pai);   }
  private boolean PV(Node pai) {return matchL(";", pai);    }
  private boolean AbCo(Node pai) {return matchL("<<", pai); }
  private boolean FeCo(Node pai) {return matchL(">>", pai); }

  // PROG -> bloco
  private boolean prog(Node pai) {
    Node no = pai.addNode("bloco");
    return bloco(no);
  }

  // BLOCO -> (comentario | print | input | atribuicao | si | declara | quantum | facere | per | experiri)*
  private boolean bloco(Node pai) {
    while (token != null && !token.lexema.equals("}") && !token.tipo.equals("EOF")) {
      Node nodeInstrucao = new Node("instrucao");

      if      (comentario(nodeInstrucao))  pai.addNode(nodeInstrucao);
      else if (scribere(nodeInstrucao))    pai.addNode(nodeInstrucao);
      else if (input(nodeInstrucao))       pai.addNode(nodeInstrucao);
      else if (si(nodeInstrucao))          pai.addNode(nodeInstrucao);
      else if (quantum(nodeInstrucao))     pai.addNode(nodeInstrucao);
      else if (facere(nodeInstrucao))      pai.addNode(nodeInstrucao);
      else if (per(nodeInstrucao))         pai.addNode(nodeInstrucao);
      else if (experiri(nodeInstrucao))    pai.addNode(nodeInstrucao);
      else if (declara(nodeInstrucao))     pai.addNode(nodeInstrucao);
      else if (atribuicao(nodeInstrucao))  pai.addNode(nodeInstrucao);
      else return false;
    }
    return true;
  }

  // DECLARA -> (assidus)? tipo ID (= expr)? ;
  private boolean declara(Node pai) {
    Node no = new Node("declara");

    if (token != null && token.lexema.equals("assidus"))
      matchL("assidus", no);

    if (!tipo(no)) return false;

    if (!ID(no)) return false;

    if (token != null && token.lexema.equals("=")) {
      matchL("=", no);
      Node noExpr = no.addNode("atribuicao");
      if (!expr(noExpr)) return false;
    }

    if (!PV(no)) return false;
    pai.addNode(no);
    return true;
  }

  // ATRIBUICAO -> ID (= expr | ++ | -- | += expr | -= expr | *= expr | /= expr) ;
  private boolean atribuicao(Node pai) {
    if (token == null || !token.tipo.equals("id")) return false;

    Node no = new Node("atribuicao");
    matchT("id", no);

    if (token != null && token.lexema.equals("=")) {
      matchL("=", no);
      Node noExpr = no.addNode("expr");
      if (!expr(noExpr)) return false;

    } else if (token != null &&
        (token.lexema.equals("++") || token.lexema.equals("--"))) {
      matchL(token.lexema, no);

    } else if (token != null &&
        (token.lexema.equals("+=") || token.lexema.equals("-=") ||
         token.lexema.equals("*=") || token.lexema.equals("/="))) {
      matchL(token.lexema, no);
      Node noExpr = no.addNode("incremento");
      if (!expr(noExpr)) return false;

    } else {
      return false;
    }

    if (!PV(no)) return false;
    pai.addNode(no);
    return true;
  }

  // SCRIBERE -> scribere ( (filum | expr) (, (filum | expr))* ) ;
  private boolean scribere(Node pai) {
    if (!matchL("scribere")) return false;
    Node no = new Node("scribere");

    if (!AP(no)) return false;

    Node arg = no.addNode("arg");
    if (!texto(arg) && !expr(arg)) return false;

    while (token != null && token.lexema.equals(",")) {
      matchL(",");
      Node argN = no.addNode("arg");
      if (!texto(argN) && !expr(argN)) return false;
    }

    if (!FP(no)) return false;
    if (!PV(no)) return false;
    pai.addNode(no);
    return true;
  }

  // INPUT -> inputus ( conteudo ) ;
  private boolean input(Node pai) {
    if (!matchL("inputus")) return false;
    Node no = new Node("input");

    if (!AP(no)) return false;
    Node noC = no.addNode("conteudo");
    if (!texto(noC) && !ID(noC)) return false;
    if (!FP(no)) return false;
    if (!PV(no)) return false;
    pai.addNode(no);
    return true;
  }

  // SI -> "si" ( condicao ) { bloco } ("alitersi" ( condicao ) { bloco })* ("nisi" { bloco })?
  private boolean si(Node pai) {
    if (!matchL("si")) return false;
    Node no = new Node("si");

    if (!AP(no)) return false;

    Node noCond = no.addNode("condicao");
    if (!condicao(noCond)) return false;
    if (!FP(no)) return false;
    if (!AC(no)) return false;
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)) return false;
    if (!FC(no)) return false;

    while (token != null && token.lexema.equals("alitersi")) {
      matchL("alitersi");
      Node noAlt = no.addNode("alitersi");
      if (!AP(no)) return false;
      Node noCondA = noAlt.addNode("condicao");
      if (!condicao(noCondA)) return false;
      if (!FP(no)) return false;
      if (!AC(no)) return false;
      Node noBlocoA = noAlt.addNode("bloco");
      if (!bloco(noBlocoA)) return false;
      if (!FC(no)) return false;
    }

    if (token != null && token.lexema.equals("nisi")) {
      matchL("nisi");
      Node noNisi = no.addNode("nisi");
      if (!AC(noNisi)) return false;

      Node noBlocoN = noNisi.addNode("bloco");
      if (!bloco(noBlocoN)) return false;
      if (!FC(noNisi)) return false;
    }

    pai.addNode(no);
    return true;
  }

  // QUANTUM -> "quantum" ( condicao | logicum ) { bloco }
  private boolean quantum(Node pai) {
    if (!matchL("quantum")) return false;
    Node no = new Node("quantum");

    if (!AP(no)) return false;
    Node noCond = no.addNode("condicao");
    if (!condicao(noCond) && !logico(noCond)) return false;
    if (!FP(no)) return false;
    if (!AC(no)) return false;
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)) return false;
    if (!FC(no)) return false;

    pai.addNode(no);
    return true;
  }

  // FACERE (do-while) -> "facere" { bloco } "quantum" ( condicao ) ;
  private boolean facere(Node pai) {
    if (!matchL("facere")) return false;
    Node no = new Node("facere");

    if (!AC(no)) return false;
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)) return false;
    if (!FC(no)) return false;
    if (!matchL("quantum")) return false;
    if (!AP(no)) return false;
    Node noCond = no.addNode("condicao");
    if (!condicao(noCond)) return false;
    if (!FP(no)) return false;
    if (!PV(no)) return false;

    pai.addNode(no);
    return true;
  }

  // PER -> "per" ( ID = expr ; condicao ; ID (++ | --) ) { bloco }
  private boolean per(Node pai) {
    if (!matchL("per")) return false;
    Node no = new Node("per");

    if (!AP(no)) return false;

    Node noInit = no.addNode("init");
    if (!ID(noInit)) return false;
    if (!matchL("=", noInit)) return false;
    Node noExprI = noInit.addNode("exprInit");
    if (!expr(noExprI)) return false;
    if (!PV(no)) return false;

    Node noCond = no.addNode("condicao");
    if (!condicao(noCond)) return false;
    if (!PV(no)) return false;

    Node noInc = no.addNode("incremento");
    if (!ID(noInc)) return false;
    if (token != null && (token.lexema.equals("++") || token.lexema.equals("--"))) {
      matchL(token.lexema, noInc);
    } else return false;

    if (!FP(no)) return false;
    if (!AC(no)) return false;
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)) return false;
    if (!FC(no)) return false;

    pai.addNode(no);
    return true;
  }

  // EXPERIRI -> "experiri" { bloco } "capere" ( ID ) { bloco }
  private boolean experiri(Node pai) {
    if (!matchL("experiri")) return false;
    Node no = new Node("experiri");

    if (!AC(no)) return false;
    Node noBT = no.addNode("bloco");
    if (!bloco(noBT)) return false;
    if (!FC(no)) return false;
    if (!matchL("capere")) return false;
    Node noCap = no.addNode("capere");
    if (!AP(no)) return false;
    if (!ID(noCap)) return false;
    if (!FP(no) || !AC(no)) return false;
    Node noBC = noCap.addNode("bloco");
    if (!bloco(noBC)) return false;
    if (!FC(no)) return false;

    pai.addNode(no);
    return true;
  }

  // COMENTARIO -> << ... >>
  private boolean comentario(Node pai) {
    if (!AbCo(pai)) return false;
    Node no = new Node("comentario");
    while (token != null && !token.lexema.equals(">>")) {
      no.addNode(token.lexema);
      token = getNextToken();
    }
    pai.addNode(no);
    if (!FeCo(pai)) return false;
    return true;
  }

  // EXPR -> termo (('+' | '-') termo)*
  private boolean expr(Node pai) {
    Node noT = new Node("termo");
    if (!termo(noT)) return false;
    pai.addNode(noT);

    while (token != null && (token.lexema.equals("+") || token.lexema.equals("-"))) {
      matchL(token.lexema, pai);
      Node noTN = new Node("termo");
      if (!termo(noTN)) return false;
      pai.addNode(noTN);
    }
    return true;
  }

  // TERMO -> fator (('*' | '/' | '%' | '//' | '**') fator)*
  private boolean termo(Node pai) {
    Node noF = new Node("fator");
    if (!fator(noF)) return false;
    pai.addNode(noF);

    while (token != null &&
        (token.lexema.equals("*")  || token.lexema.equals("/") ||
         token.lexema.equals("%")  || token.lexema.equals("//") ||
         token.lexema.equals("**"))) {
      matchL(token.lexema, pai);
      Node noFN = new Node("fator");
      if (!fator(noFN)) return false;
      pai.addNode(noFN);
    }
    return true;
  }

  // FATOR -> LITTOTUM | LITFRACTUM | LITFILUM | LITCHAR | LITLOGICUM | ID | ( expr )
  private boolean fator(Node pai) {
    if (matchT("LITTOTUM",   pai)) return true;
    if (matchT("LITFRACTUM", pai)) return true;
    if (matchT("LITFILUM",   pai)) return true;
    if (matchT("LITCHAR",    pai)) return true;
    if (matchT("LITLOGICUM", pai)) return true;
    if (ID(pai))                   return true;
    if (AP(pai)) {
      Node noE = pai.addNode("expr");
      if (!expr(noE) || !FP(pai)) return false;
      return true;
    }
    return false;
  }

  private boolean condicao(Node pai)         { return condicao_logica(pai); }

  // CONDICAO_LOGICA -> condicao_relacional (&& | || condicao_relacional)*
  private boolean condicao_logica(Node pai) {
    Node nodeR = new Node("cond_logica");
    if (!condicao_relacional(nodeR)) return false;
    pai.addNode(nodeR);

    while (token != null && (token.lexema.equals("&&") || token.lexema.equals("||"))) {
      matchL(token.lexema, pai);
      Node nodeR2 = new Node("cond_logica");
      if (!condicao_relacional(nodeR2)) return false;
      pai.addNode(nodeR2);
    }
    return true;
  }

  // CONDICAO_RELACIONAL -> expr op_rel expr
  private boolean condicao_relacional(Node pai) {
    Node noExpr1 = new Node("expr");
    if (!expr(noExpr1)) return false;
    pai.addNode(noExpr1);

    if (token == null) return false;

    String op = token.lexema;
    if (!op.equals("<") && !op.equals(">") && !op.equals("<=") &&
        !op.equals(">=") && !op.equals("==") && !op.equals("!="))
      return false;

    matchL(op, pai);
    Node noExpr2 = new Node("expr");
    if (!expr(noExpr2)) return false;
    pai.addNode(noExpr2);
    return true;
  }

  private boolean tipo(Node pai) {
    if (token != null && (
      token.lexema.equals("totum")   ||
      token.lexema.equals("fractum") ||
      token.lexema.equals("logicum") ||
      token.lexema.equals("filum")   ||
      token.lexema.equals("char"))) {
      pai.addNode("tipo(" + token.lexema + ")");
      token = getNextToken();
      return true;
    }
    return false;
  }

  private boolean ID(Node pai)     { return matchT("id",         pai); }
  private boolean logico(Node pai) { return matchT("LITLOGICUM", pai); }
  private boolean texto(Node pai)  { return matchT("LITFILUM",   pai); }
}
