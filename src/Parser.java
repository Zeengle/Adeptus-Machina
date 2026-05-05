import java.util.List;

public class Parser {

  List<Token> tokens;
  Token token;
  Tree tree;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.tree = new Tree();
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
    if (tokens.size() > 0) 
      return tokens.remove(0);
    return null;
  }

  //  MATCH 
  private boolean matchL(String palavra, Node pai) {
    if (token != null && token.lexema.equals(palavra)) {
      if (pai != null) 
      pai.addNode(palavra);
      token = getNextToken();
      return true;
    }
    return false;
  }

  private boolean matchT(String tipo, Node pai) {
    if (token != null && token.tipo.equals(tipo)) {
      if (pai != null) 
        pai.addNode(tipo + "(" + token.lexema + ")");
      token = getNextToken();
      return true;
    }
    return false;
  }


  private boolean matchL(String palavra){ 
    return matchL(palavra, null); 
  }
  private boolean matchT(String tipo){ 
    return matchT(tipo, null);    
  }

  //  ERRO
  private void erro() {
    if (token != null)
      System.out.println("Erro sintatico: '" + token.lexema + "' (tipo: " + token.tipo + ")");
    else
      System.out.println("Erro: fim inesperado da entrada");
  }

  //  PROG ➜ bloco
  private boolean prog(Node pai) {
    Node no = pai.addNode("bloco");
    return bloco(no);
  }

  //  BLOCO ➜ (print | input | atribuicao | si | declara | quantum | per | experiri)*
  private boolean bloco(Node pai) {
    while (token != null && !token.lexema.equals("}") && !token.tipo.equals("EOF")) {
      Node nodeInstrucao = new Node("instrucao");

      if(comentario(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (scribere(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (input(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (si(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (quantum(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (per(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (experiri(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (declara(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else if (atribuicao(nodeInstrucao))
        pai.addNode(nodeInstrucao);
      else {
        return false; 
      } 
    }
    return true;
  }

  //  DECLARA ➜ (assidus)? tipo ID (op_igualdade expr)? fim_linha
  private boolean declara(Node pai) {
    Node no = new Node("declara");

    if (token != null && token.lexema.equals("assidus"))
      matchL("assidus", no);

    if (!tipo(no)) return false;

    if (!ID(no)){ 
      return false; 
    }

    if (token != null && token.lexema.equals("=")) {
      matchL("=", no);
      Node noExpr = no.addNode("atribuicao");
      if (!expr(noExpr)){ 
        return false; 
      }
    }

    if (!matchL(";")){ 
      return false; 
    }
    pai.addNode(no);
    return true;
  }

  //  ATRIBUICAO ➜ ID (op_igualdade expr | op_atrib | op_inc expr) fim_linha
  private boolean atribuicao(Node pai) {
    if (token == null || !token.tipo.equals("id")) 
      return false;

    Node no = new Node("atribuicao");
    matchT("id", no);

    if (token != null && token.lexema.equals("=")) {
      matchL("=", no);
      Node noExpr = no.addNode("atribuicao");
      if (!expr(noExpr)){ 
        return false; 
      }

    } else if (token != null &&
        (token.lexema.equals("++") || token.lexema.equals("--"))) {
      matchL(token.lexema, no);

    } else if (token != null &&
        (token.lexema.equals("+=") || token.lexema.equals("-=") ||
         token.lexema.equals("*=") || token.lexema.equals("/="))) {
      matchL(token.lexema, no);
      Node noExpr = no.addNode("incremento");
      if (!expr(noExpr)){ 
        return false; 
      }

    } else {
      return false;
    }

    if (!matchL(";")){ 
      return false; 
    }
    pai.addNode(no);
    return true;
  }

  //  SCRIBERE ➜ scribere AP (filum | expr) (virgula (filum | expr))* FP fim_linha
  private boolean scribere(Node pai) {
    if (!matchL("scribere")) 
      return false; 
    Node no = new Node("scribere");

    if (!matchL("(")){ 
      return false; 
    }

    Node arg = no.addNode("arg");
    if (!texto(arg) && !expr(arg)){ 
      return false; 
    }

    while (token != null && token.lexema.equals(",")) {
      matchL(",");
      Node argN = no.addNode("arg");
      if (!texto(argN) && !expr(argN)){ 
        return false; 
      }
    }

    if (!matchL(")")){ 
      return false; 
    }
    if (!matchL(";")){ 
      return false; 
    }
    pai.addNode(no);
    return true;
  }

  //  INPUT ➜ inputus AP conteudo FP fim_linha
  private boolean input(Node pai) {
    if (!matchL("inputus")) 
      return false;
    Node no = new Node("input");

    if (!matchL("(")){ 
      return false; 
    }
    Node noC = no.addNode("conteudo");
    if (!texto(noC) && !ID(noC)){ 
      return false; 
    }
    if (!matchL(")")){ 
      return false; 
    }
    if (!matchL(";")){ 
      return false; 
    }
    pai.addNode(no);
    return true;
  }

  /* 
      SI ➜ "si" AP condicao FP AC bloco FC ("alitersi" AP condicao FP AC bloco FC)*("nisi" AC bloco FC)?
  */
   private boolean si(Node pai) {
    if (!matchL("si")) 
      return false;
    Node no = new Node("si");

    if (!matchL("(")){ 
      return false; 
    }
    
    Node noCond = no.addNode("condicao");
    if (!condicao(noCond)){ 
      return false; 
    }
    if (!matchL(")")){ 
      return false; 
    }
    if (!matchL("{")){ 
      return false; 
    }

    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)){ 
      return false; 
    }
    if (!matchL("}")){ 
      return false; 
    }

    while (token != null && token.lexema.equals("alitersi")) {
      matchL("alitersi");
      
      Node noAlt = no.addNode("alitersi");
      if (!matchL("(")){ 
        return false; 
      }

      Node noCondA = noAlt.addNode("condicao");
      if (!condicao(noCondA)){ 
        return false; 
      }
      if (!matchL(")")){ 
        return false; 
      }
      if (!matchL("{")){ 
        return false; 
      }
      Node noBlocoA = noAlt.addNode("bloco");
      if (!bloco(noBlocoA)){ 
        return false; 
      }
      if (!matchL("}")){ 
        return false; 
      }
    }

    if (token != null && token.lexema.equals("nisi")) {
      matchL("nisi");

      Node noNisi = no.addNode("nisi");
      if (!matchL("{")){ 
        return false; 
      }

      Node noBlocoN = noNisi.addNode("bloco");
      if (!bloco(noBlocoN)){ 
        return false; 
      }
      if (!matchL("}")){ 
        return false; 
      }
    }

    pai.addNode(no);
    return true;
  }

  //  QUANTUM ➜ "quantum" AP (condicao | logicum) FP AC bloco FC
  private boolean quantum(Node pai) {
    if (!matchL("quantum")) 
      return false;
    Node no = new Node("quantum");

    if (!matchL("(")){ 
      return false; 
    }
    Node noCond = no.addNode("condicao");
    if (!condicao(noCond) && !logico(noCond)) { 
      return false; 
    }
    if (!matchL(")")) { 
      return false; 
    }
    if (!matchL("{")){ 
      return false; 
    }
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)){ 
      return false; 
    }
    if (!matchL("}")){ 
      return false; 
    }

    pai.addNode(no);
    return true;
  }

  //  PER ➜ "per" AP atribuicao_base fim_linha condicao fim_linha atribuicao_fim FP AC bloco FC
  private boolean per(Node pai) {
    if (!matchL("per")) 
      return false;
    Node no = new Node("per");

    if (!matchL("(")) { 
      return false; 
    }

    Node noInit = no.addNode("init");
    if (!ID(noInit)){ 
      return false; 
    }
    if (!matchL("=", noInit)){ 
      return false; 
    }
    Node noExprI = noInit.addNode("exprInit");
    if (!expr(noExprI)){ 
      return false; 
    }
    if (!matchL(";")){ 
      return false; 
    }

    Node noCond = no.addNode("condicao");
    if (!condicao(noCond)){ 
      return false; 
    }
    if (!matchL(";")){ 
      return false; 
    }

    Node noInc = no.addNode("incremento");
    if (!ID(noInc)){ 
      return false; 
    }
    if (token != null && (token.lexema.equals("++") || token.lexema.equals("--"))) {
      matchL(token.lexema, noInc);
    } else { 
      return false; 
    }

    if (!matchL(")")) { 
      return false; 
    }
    if (!matchL("{")){ 
      return false; 
    }
    Node noBloco = no.addNode("bloco");
    if (!bloco(noBloco)) { 
      return false; 
    }
    if (!matchL("}"))    { 
      return false; 
    }

    pai.addNode(no);
    return true;
  }

  //  EXPERIRI ➜ "experiri" AC bloco FC "capere" AP ID FP AC bloco FC
  private boolean experiri(Node pai) {
    if (!matchL("experiri")) 
      return false;
    Node no = new Node("experiri");

    if (!matchL("{")){ 
      return false; 
    }
    Node noBT = no.addNode("bloco");
    if (!bloco(noBT)){ 
      return false; 
    }
    if (!matchL("}")){ 
      return false; 
    }
    if (!matchL("capere")) { 
      return false; 
    }
    Node noCap = no.addNode("capere");
    if (!matchL("(")){ 
      return false; 
    }
    if (!ID(noCap)){ 
      return false; 
    }
    if (!matchL(")") || !matchL("{")) { 
      return false; }
    Node noBC = noCap.addNode("bloco");
    if (!bloco(noBC)){
      return false; 
    }
    if (!matchL("}")){ 
      return false; 
    }

    pai.addNode(no);
    return true;
  }

  //  COMENTARIO
  private boolean comentario(Node pai) {
    if (!matchL("<<")) return false;
    Node no = new Node("comentario");
    while (token != null && !token.lexema.equals(">>")) {
      no.addNode(token.lexema);
      token = getNextToken();
    }
    if (!matchL(">>")) { 
      return false; 
    }
    pai.addNode(no);
    return true;
  }

  //  EXPR ➜ termo (('+' | '-') termo)*
  private boolean expr(Node pai) {
    Node noT = new Node("termo");
    if (!termo(noT)) 
      return false;
    pai.addNode(noT);

    while (token != null &&
        (token.lexema.equals("+") || token.lexema.equals("-"))) {
      matchL(token.lexema, pai);     
      Node noTN = new Node("termo");
      if (!termo(noTN)) { 
        return false; 
      }
      pai.addNode(noTN);
    }
    return true;
  }

  //  TERMO ➜  fator (('*' | '/' | '%' | '//' | '**') fator)*
  private boolean termo(Node pai) {
    Node noF = new Node("fator");
    if (!fator(noF)) 
      return false;
    pai.addNode(noF);

    while (token != null &&
        (token.lexema.equals("*")  || token.lexema.equals("/") ||
         token.lexema.equals("%")  || token.lexema.equals("//") ||
         token.lexema.equals("**"))) {
      matchL(token.lexema, pai);      
      Node noFN = new Node("fator");
      if (!fator(noFN)) { 
        return false; 
      }
      pai.addNode(noFN);
    }
    return true;
  }

  //  FATOR ➜ tipo | ID | AP expr FP
  private boolean fator(Node pai) {
    if (matchT("LITTOTUM", pai)) 
      return true;   
      if (matchT("LITFRACTUM", pai)) 
        return true; 
      if (matchT("LITFILUM", pai)) 
        return true; 
      if (matchT("LITCHAR", pai)) 
        return true;
      if (matchT("LITLOGICUM", pai)) 
        return true;
      if (ID(pai)) 
        return true;
      if (matchL("(")) {
          Node noE = pai.addNode("expr");
          if (!expr(noE) || !matchL(")")) 
            return false;
          return true;
      }
      return false;
  }


  //  CONDICOES
  private boolean condicao(Node pai) { 
    return condicao_logica(pai); 
  }


  //  op_logicos ➜ '&&' | '||'
  private boolean condicao_logica(Node pai) {
    Node nodecondrelacional = new Node("cond_logica");
    if (!condicao_relacional(nodecondrelacional)) 
      return false;
    pai.addNode(nodecondrelacional);

    while (token != null &&
        (token.lexema.equals("&&") || token.lexema.equals("||"))) {
      matchL(token.lexema, pai);
      Node nodecondlogica = new Node("cond_logica");
      if (!condicao_relacional(nodecondlogica)) { 
        return false; 
      }
      pai.addNode(nodecondlogica);
    }
    return true;
  }

  //  op_relacional ➜ '<' | '>' | '<=' | '>=' | '==' | '!='
  
  private boolean condicao_relacional(Node pai) {
    Node noExpr1 = new Node("expr");
    if (!expr(noExpr1)) 
      return false;
    pai.addNode(noExpr1);

    if (token == null) { 
      return false; 
    }

    String op = token.lexema;
    if (!op.equals("<") && !op.equals(">") && !op.equals("<=") &&
        !op.equals(">=") && !op.equals("==") && !op.equals("!=")) {
      return false;
    }
    matchL(op, pai);                  

    Node noExpr2 = new Node("expr");
    if (!expr(noExpr2)) { 
      return false; }
    pai.addNode(noExpr2);
    return true;
  }


  private boolean tipo(Node pai) {
    if (token != null && (
      token.lexema.equals("totum") || 
      token.lexema.equals("fractum") ||
      token.lexema.equals("logicum") ||
      token.lexema.equals("filum") ||
      token.lexema.equals("char")
    )) {
    pai.addNode("tipo(" + token.lexema + ")");
    token = getNextToken();
    return true;
    }
    return false;
  }

  private boolean ID(Node pai){ 
    return matchT("id", pai);      
  }
  private boolean logico(Node pai){ 
    return matchT("LITLOGICUM", pai); 
  }
  private boolean texto(Node pai){ 
    return matchT("LITFILUM", pai);
  }
}