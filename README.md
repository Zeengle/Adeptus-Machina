# Adeptus-Machina

Adeptus-Machina é um projeto de compilador desenvolvido para o curso *CCM510 – Compiladores*.

O objetivo deste projeto é projetar e implementar uma linguagem de programação baseada em latim, inspirada na sintaxe e estrutura de Python e C/C++.

---

## 📌 Visão Geral

Adeptus-Machina explora a construção de uma linguagem de programação utilizando o latim como sua base. O projeto foca nas principais etapas da criação do front-end de um compilador, incluindo o Analisador Léxico, Sintático e Semântico.

A linguagem combina terminologia clássica com paradigmas modernos de programação, oferecendo suporte à programação estruturada, variáveis tipadas e mecanismos de controle de fluxo.

---

## 🚀 Funcionalidades

* Palavras-chave baseadas em latim  
* Sistema de tipagem estática  
* Expressões aritméticas e lógicas  
* Estruturas de controle (`if`, `while`, `for`)  
* Operações de entrada e saída  
* Suporte a tratamento de exceções (estrutura similar a `try/catch`)  

---

## 🔤 Estrutura Léxica

| Palavra-chave comum | Adeptus-Machina |
| ------------------ | --------------- |
| import             | `importus`      |
| export             | `exportus`      |
| class              | `classis`       |
| input              | `inputus`       |
| print              | `scribere`      |
| true               | `VERUM`         |
| false              | `FALSUM`        |
| null               | `NIHIL`         |
| integer            | `totum`         |
| float              | `fractum`       |
| boolean            | `logicum`       |
| string             | `filum`         |
| char               | `char`          |
| constant           | `assidus`       |
| if                 | `si`            |
| else               | `nisi`          |
| elif               | `alitersi`      |
| while              | `quantum`       |
| do                 | `facere`        |
| for                | `per`           |
| break              | `rumpere`       |
| continue           | `continuare`    |
| return             | `reddere`       |
| try                | `experiri`      |
| catch              | `capere`        |

Os seguintes tokens definem os elementos primitivos da linguagem:

```txt
totum    ➜ [0-9]+
fractum  ➜ [0-9]+ '.' [0-9]+
char     ➜ '[a-zA-Z0-9 \t\n.?_!]'
filum    ➜ "[a-zA-Z0-9 \t\n.?_!:]*"
logicum  ➜ ('VERUM' | 'FALSUM')
```

## Símbolos

```txt
AP            ➜ (   // Abre Parênteses
FP            ➜ )   // Fecha Parênteses
AC            ➜ {   // Abre Chaves
FC            ➜ }   // Fecha Chaves
op_igualdade  ➜ =
op_relacional ➜ '<' | '>' | '<=' | '>=' | '!=' | '=='
op_inc        ➜ '+=' | '-=' | '*=' | '/='
op_atrib      ➜ '++' | '--'
fim_linha     ➜ ';'
AComentario   ➜ '<<'
FComentario   ➜ '>>'
virgula       ➜ ','
comentario    ➜ AComentario .* FComentario
```

## 🧠 Sintaxe e Gramática

A gramática define a estrutura de programas válidos em Adeptus-Machina.

```txt
prog ➜ bloco

tipo ➜ (totum | fractum | filum | char | logicum)
ID ➜ [a-z][a-z0-9]*

declara ➜ (assidus)? tipo ID (op_igualdade expr)? fim_linha

expr ➜ termo (('+' | '-') termo)*
termo ➜ fator (('*' | '/' | '%' | '//' | '**') fator)*
fator ➜ ID | AP expr FP

conteudo ➜ filum | ID

print ➜ scribere AP (filum | expr) (virgula (filum | expr))* FP fim_linha
input ➜ inputus AP conteudo FP fim_linha

atribuicao_base ➜ ID op_igualdade expr
atribuicao_fim ➜ ID op_atrib
atribuicao ➜ ID (op_igualdade expr | op_atrib | op_inc expr) fim_linha

op_logicos ➜ && | ||

bloco ➜ (print | input | atribuicao | if | declara | while | for | try)*

condicao ➜ condicao_logica
condicao_logica ➜ condicao_relacional (op_logicos condicao_relacional)*
condicao_relacional ➜ expr op_relacional expr

if ➜ 'si' AP condicao FP AC bloco 
      ('alitersi' AP condicao FP AC bloco FC)* 
      ('nisi' AC bloco FC)?

while ➜ 'quantum' AP (condicao | logicum) FP AC bloco FC

for ➜ 'per' AP atribuicao_base fim_linha condicao fim_linha atribuicao_fim FP AC bloco FC 

try ➜ 'experiri' AC bloco FC 
       'capere' AP ID FP AC bloco FC
```

## 🧪 Exemplo

Abaixo está um exemplo simples demonstrando a sintaxe da linguagem:

```java

totum x = 10;

si (x > 5) {
    scribere("Maior que cinco");
} nisi {
    scribere("Menor ou igual a cinco");
}

```

## 🎯 Decisões de Design

* Palavras-chave em latim: Escolhidas para dar uma identidade única à linguagem, mantendo significado semântico.
* Sintaxe estruturada: Inspirada em Python e C/C++ para garantir familiaridade e legibilidade.
* Tipagem estática: Garante segurança de tipos e clareza durante a compilação.
* Gramática personalizada: Projetada para suportar construções essenciais de programação de forma concisa.

## 📚 Propósito Educacional

Este projeto foi desenvolvido como parte de um curso de construção de compiladores, com o objetivo de aplicar conceitos teóricos como:

* Criação de Tokens
* Parsing (análise sintática)
* Definição de gramática
* Design de linguagens
