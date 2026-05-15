import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IdeMachina extends JFrame {

    private JTextPane editorCodigo;
    private JTextArea gutterLinhas;
    private JTextPane consoleOutput;
    private JTextPane analysisOutput;
    private JLabel statusBar;
    private JTextField inputField;
    private JButton btnEnviar;
    private PascalRunner runner;

    // Paleta One Dark Pro
    private final Color BG_COLOR = new Color(40, 44, 52);
    private final Color EDITOR_BG = new Color(33, 37, 43);
    private final Color GUTTER_BG = new Color(40, 44, 52);
    private final Color TEXT_COLOR = new Color(171, 178, 191);
    private final Color ACCENT_COLOR = new Color(97, 175, 239);
    private final Color ERROR_COLOR = new Color(224, 108, 117);
    private final Color SUCCESS_COLOR = new Color(152, 195, 121);
    private final Color PURPLE_COLOR = new Color(198, 120, 221);
    private final Color VAR_COLOR = new Color(0, 211, 255);
    private final Color BOOL_COLOR = new Color(255, 255, 0);
    private final Color PANEL_BG = new Color(24, 26, 31);

    public IdeMachina() {
        setTitle("Adeptus-Machina IDE - CC510 2.0");
        setSize(1150, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        UIManager.put("ScrollBar.thumb", new Color(76, 83, 99));
        UIManager.put("ScrollBar.track", EDITOR_BG);
        UIManager.put("TabbedPane.background", BG_COLOR);
        UIManager.put("TabbedPane.foreground", TEXT_COLOR);
        UIManager.put("TabbedPane.contentAreaColor", PANEL_BG);
        UIManager.put("TabbedPane.selected", EDITOR_BG);

        add(criarToolbar(), BorderLayout.NORTH);
        add(criarAreaCentral(), BorderLayout.CENTER);
        add(criarStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel criarToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.setBackground(BG_COLOR);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(24,26,31)));

        JButton btnCompilar = new JButton("▶ Run Adeptus-Machina");
        btnCompilar.setBackground(ACCENT_COLOR);
        btnCompilar.setForeground(Color.BLACK);
        btnCompilar.setFocusPainted(false);
        btnCompilar.setBorder(new EmptyBorder(8, 15, 8, 15));
        btnCompilar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCompilar.addActionListener(e -> executarCompilador());

        toolbar.add(btnCompilar);
        return toolbar;
    }

    private JSplitPane criarAreaCentral() {

        // ================= EDITOR =================
        editorCodigo = new JTextPane();
        editorCodigo.setBackground(EDITOR_BG);
        editorCodigo.setForeground(TEXT_COLOR);
        editorCodigo.setCaretColor(ACCENT_COLOR);
        editorCodigo.setFont(new Font("Consolas", Font.PLAIN, 15));
        editorCodigo.setMargin(new Insets(8, 12, 8, 12));

        configurarComportamentoInteligenteDoEditor();

        editorCodigo.setText(
                "<<\"Comentário\">>\n" +
                "totum contador;\n" +
                "per (contador = 1; contador <= 10; contador++) {\n" +
                "    scribere(\"Valor:\", contador);\n" +
                "}\n"
        );

        aplicarCoresSintaxe();

        // ================= GUTTER =================
        gutterLinhas = new JTextArea("1");
        gutterLinhas.setBackground(GUTTER_BG);
        gutterLinhas.setForeground(new Color(92, 99, 112));
        gutterLinhas.setFont(new Font("Consolas", Font.PLAIN, 15));
        gutterLinhas.setEditable(false);
        gutterLinhas.setMargin(new Insets(8, 5, 8, 5));
        gutterLinhas.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(76,83,99)));

        editorCodigo.getDocument().addDocumentListener(new DocumentListener() {
            public void atualizar() {
                int lines = editorCodigo.getText().split("\n", -1).length;
                StringBuilder text = new StringBuilder();
                for (int i = 1; i <= lines; i++) text.append(i).append("\n");
                gutterLinhas.setText(text.toString());
                SwingUtilities.invokeLater(() -> aplicarCoresSintaxe());
            }
            public void insertUpdate(DocumentEvent e) { atualizar(); }
            public void removeUpdate(DocumentEvent e) { atualizar(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        JPanel painelEditor = new JPanel(new BorderLayout());
        painelEditor.add(gutterLinhas, BorderLayout.WEST);
        painelEditor.add(editorCodigo, BorderLayout.CENTER);

        JScrollPane scrollEditor = new JScrollPane(painelEditor);
        scrollEditor.setBorder(null);

        // ================= TERMINAL =================
        consoleOutput = new JTextPane();
        consoleOutput.setBackground(PANEL_BG);
        consoleOutput.setForeground(TEXT_COLOR);
        consoleOutput.setFont(new Font("Consolas", Font.PLAIN, 14));
        consoleOutput.setEditable(false);
        consoleOutput.setMargin(new Insets(10,10,10,10));

        JScrollPane scrollConsole = new JScrollPane(consoleOutput);
        scrollConsole.setBorder(null);
        scrollConsole.getViewport().setBackground(PANEL_BG);

        // ================= INPUT INTERATIVO =================
        inputField = new JTextField();
        inputField.setBackground(new Color(33, 37, 43));
        inputField.setForeground(TEXT_COLOR);
        inputField.setFont(new Font("Consolas", Font.PLAIN, 14));
        inputField.setCaretColor(TEXT_COLOR);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, ACCENT_COLOR),
            new EmptyBorder(6, 10, 6, 10)
        ));
        inputField.setEnabled(false);

        btnEnviar = new JButton("Enviar");
        btnEnviar.setBackground(ACCENT_COLOR);
        btnEnviar.setForeground(Color.BLACK);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorder(new EmptyBorder(6, 12, 6, 12));
        btnEnviar.setEnabled(false);

        Runnable enviarEntrada = () -> {
            String texto = inputField.getText();
            if (runner != null && runner.emExecucao()) {
                append(consoleOutput, texto + "\n", SUCCESS_COLOR);
                runner.enviarEntrada(texto);
                inputField.setText("");
            }
        };

        btnEnviar.addActionListener(e -> enviarEntrada.run());
        inputField.addActionListener(e -> enviarEntrada.run());

        JPanel painelInput = new JPanel(new BorderLayout());
        painelInput.setBackground(PANEL_BG);
        painelInput.add(inputField, BorderLayout.CENTER);
        painelInput.add(btnEnviar, BorderLayout.EAST);

        JPanel painelConsole = new JPanel(new BorderLayout());
        painelConsole.setBackground(PANEL_BG);
        painelConsole.add(scrollConsole, BorderLayout.CENTER);
        painelConsole.add(painelInput, BorderLayout.SOUTH);

        // ================= ANALYSIS =================
        analysisOutput = new JTextPane();
        analysisOutput.setBackground(PANEL_BG);
        analysisOutput.setForeground(PURPLE_COLOR);
        analysisOutput.setFont(new Font("Consolas", Font.PLAIN, 13));
        analysisOutput.setEditable(false);
        analysisOutput.setMargin(new Insets(10,10,10,10));

        JScrollPane scrollAnalysis = new JScrollPane(analysisOutput);
        scrollAnalysis.setBorder(null);
        scrollAnalysis.getViewport().setBackground(PANEL_BG);

        // ================= TABS =================
        JTabbedPane painelAbas = new JTabbedPane();
        painelAbas.setBorder(BorderFactory.createMatteBorder(0,1,0,0, ACCENT_COLOR));
        painelAbas.addTab("💻 Terminal", painelConsole);
        painelAbas.addTab("🔍 Análise Interna", scrollAnalysis);

        // ================= SPLIT =================
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollEditor, painelAbas);
        split.setDividerLocation(600);
        split.setDividerSize(5);
        split.setBorder(null);

        return split;
    }

    // ================= SYNTAX =================
    private void aplicarCoresSintaxe() {
        StyledDocument doc = editorCodigo.getStyledDocument();
        String texto = editorCodigo.getText();

        doc.setCharacterAttributes(0, texto.length(),
                StyleContext.getDefaultStyleContext().getEmptySet(), true);

        pintar(doc, "\\b(per|scribere|facere|si|alitersi|nisi|quantum|inputus|assidus|experiri|capere|rumpere|continuare|reddere)\\b", PURPLE_COLOR);
        pintar(doc, "\".*?\"", SUCCESS_COLOR);
        pintar(doc, "\"", ACCENT_COLOR);
        pintar(doc, "logicum|totum|fractum|filum|char", VAR_COLOR);
        pintar(doc, "FALSUM|VERUM", BOOL_COLOR);
        pintar(doc, ";", ERROR_COLOR);
        pintar(doc, "<<.*?>>", new Color(92, 99, 112));
    }

    private void pintar(StyledDocument doc, String regex, Color cor) {
        try {
            Matcher m = Pattern.compile(regex).matcher(doc.getText(0, doc.getLength()));
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, cor);

            while (m.find()) {
                doc.setCharacterAttributes(m.start(), m.end() - m.start(), style, false);
            }
        } catch (Exception ignored) {}
    }

    // ================= COMPILADOR =================
    private void executarCompilador() {
        String codigo = editorCodigo.getText();
        consoleOutput.setText("");
        analysisOutput.setText("");
        statusBar.setText("Processando...");
        statusBar.setForeground(ACCENT_COLOR);

        if (runner != null) runner.encerrar();
        inputField.setEnabled(false);
        btnEnviar.setEnabled(false);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos));

        boolean sintaticaOk = false;
        boolean semanticaOk = false;

        try {
            append(analysisOutput, "--- FASE LEXICA ---\n", PURPLE_COLOR);
            List<Token> tokensParser = Scanner.lex(codigo);
            for (Token t : tokensParser) append(analysisOutput, t.toString() + "\n", TEXT_COLOR);

            append(analysisOutput, "\n--- FASE SINTATICA ---\n", PURPLE_COLOR);
            Parser parser = new Parser(tokensParser);
            parser.main();
            System.out.flush();
            append(analysisOutput, baos.toString(), TEXT_COLOR);
            sintaticaOk = true;

            append(analysisOutput, "\n--- FASE SEMANTICA ---\n", PURPLE_COLOR);
            List<Token> tokensSem = Scanner.lex(codigo);
            Token.limparTokens();
            SemanticAnalyzer sem = new SemanticAnalyzer(tokensSem);
            semanticaOk = sem.analisar();

            for (String av : sem.getAvisos())
                append(analysisOutput, av + "\n", new Color(229, 192, 123));
            for (String er : sem.getErros())
                append(analysisOutput, er + "\n", ERROR_COLOR);

            if (sem.getErros().isEmpty() && sem.getAvisos().isEmpty())
                append(analysisOutput, "Semanticamente correta.\n", SUCCESS_COLOR);

            if (semanticaOk) {
                append(consoleOutput, "Compilado com sucesso\n", SUCCESS_COLOR);
                statusBar.setText("Sucesso");
                statusBar.setForeground(SUCCESS_COLOR);
            } else {
                append(consoleOutput, "Erros semanticos encontrados.\n", ERROR_COLOR);
                statusBar.setText("Erro semantico");
                statusBar.setForeground(ERROR_COLOR);
            }

        } catch (Exception ex) {
            append(consoleOutput, "ERRO: " + ex.getMessage(), ERROR_COLOR);
            statusBar.setText("Erro");
            statusBar.setForeground(ERROR_COLOR);
        } finally {
            System.setOut(oldOut);
        }

        if (sintaticaOk && semanticaOk) {
            List<Token> tokensTrad = Scanner.lex(codigo);
            Token.limparTokens();
            Translator translator = new Translator(tokensTrad);
            String pascal = translator.traduzir();
            translator.salvar(pascal, "output.pas");

            append(consoleOutput, "\n--- Codigo Pascal ---\n", ACCENT_COLOR);
            append(consoleOutput, pascal, TEXT_COLOR);

            runner = new PascalRunner();
            String erroCompile = runner.compilar("output.pas");
            if (erroCompile != null) {
                append(consoleOutput, "\n[Erro FPC]\n" + erroCompile, ERROR_COLOR);
                return;
            }

            append(consoleOutput, "\n--- Execucao ---\n", ACCENT_COLOR);
            inputField.setEnabled(true);
            btnEnviar.setEnabled(true);
            inputField.requestFocus();

            runner.iniciarExecucao("output.pas", linha -> {
                SwingUtilities.invokeLater(() -> {
                    append(consoleOutput, linha, TEXT_COLOR);
                    if (linha.contains("[Programa encerrado]")) {
                        inputField.setEnabled(false);
                        btnEnviar.setEnabled(false);
                        statusBar.setText("Execucao concluida");
                    }
                });
            });
        }
    }

    private void append(JTextPane pane, String msg, Color color) {
        try {
            StyledDocument doc = pane.getStyledDocument();
            SimpleAttributeSet style = new SimpleAttributeSet();
            StyleConstants.setForeground(style, color);
            doc.insertString(doc.getLength(), msg, style);
        } catch (Exception ignored) {}
    }

    // ================= EDITOR =================
    private void configurarComportamentoInteligenteDoEditor() {
        editorCodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                String insert = (c == '{') ? "}" :
                        (c == '(') ? ")" :
                        (c == '[') ? "]" :
                        (c == '"') ? "\"" : "";

                if (!insert.isEmpty()) {
                    try {
                        int pos = editorCodigo.getCaretPosition();
                        editorCodigo.getDocument().insertString(pos, insert, null);
                        editorCodigo.setCaretPosition(pos);
                    } catch (Exception ignored) {}
                }
            }
        });

        Action defaultEnter = editorCodigo.getActionMap().get(DefaultEditorKit.insertBreakAction);
        editorCodigo.getActionMap().put(DefaultEditorKit.insertBreakAction, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                try {
                    int pos = editorCodigo.getCaretPosition();
                    Element root = editorCodigo.getDocument().getDefaultRootElement();
                    int line = root.getElementIndex(pos);
                    Element elem = root.getElement(line);
                    int start = elem.getStartOffset();
                    String text = editorCodigo.getText(start, pos - start);

                    String indent = text.replaceAll("^([ \\t]*).*$", "$1");
                    boolean abre = text.trim().endsWith("{");

                    defaultEnter.actionPerformed(e);

                    if (abre) {
                        editorCodigo.getDocument().insertString(editorCodigo.getCaretPosition(), indent + "    ", null);
                    } else {
                        editorCodigo.getDocument().insertString(editorCodigo.getCaretPosition(), indent, null);
                    }

                } catch (Exception ex) {
                    defaultEnter.actionPerformed(e);
                }
            }
        });
    }

    private JPanel criarStatusBar() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(33,37,43));
        p.setBorder(new EmptyBorder(5,10,5,10));

        statusBar = new JLabel("✅ Pronto.");
        statusBar.setForeground(TEXT_COLOR);

        JLabel right = new JLabel("UTF-8  |  Latim");
        right.setForeground(new Color(92,99,112));

        p.add(statusBar, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);

        return p;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new IdeMachina().setVisible(true));
    }
}