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

    // Paleta One Dark Pro
    private final Color BG_COLOR = new Color(40, 44, 52);
    private final Color EDITOR_BG = new Color(33, 37, 43);
    private final Color GUTTER_BG = new Color(40, 44, 52);
    private final Color TEXT_COLOR = new Color(171, 178, 191);
    private final Color ACCENT_COLOR = new Color(97, 175, 239);
    private final Color ERROR_COLOR = new Color(224, 108, 117);
    private final Color SUCCESS_COLOR = new Color(152, 195, 121);
    private final Color PURPLE_COLOR = new Color(198, 120, 221);
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
                "logicum contador;\n" +
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
        painelAbas.addTab("💻 Terminal", scrollConsole);
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

        pintar(doc, "\\b(per|scribere|logicum|quatum|si|ita|reverti|falsum|verum|quantum)\\b", PURPLE_COLOR);
        pintar(doc, "\".*?\"", SUCCESS_COLOR);
        pintar(doc, "\"", ACCENT_COLOR);
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

        statusBar.setText("⏳ Processando...");
        statusBar.setForeground(ACCENT_COLOR);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream old = System.out;
        System.setOut(new PrintStream(baos));

        try {
            append(analysisOutput, "--- FASE LÉXICA ---\n", PURPLE_COLOR);
            List<Token> tokens = Scanner.lex(codigo);

            for (Token t : tokens) {
                append(analysisOutput, t.toString() + "\n", TEXT_COLOR);
            }

            append(analysisOutput, "\n--- FASE SINTÁTICA ---\n", PURPLE_COLOR);
            Parser parser = new Parser(tokens);
            parser.main();

            System.out.flush();
            append(analysisOutput, baos.toString(), TEXT_COLOR);

            append(consoleOutput, "✔ Compilado com sucesso\n", SUCCESS_COLOR);
            statusBar.setText("✅ Sucesso");
            statusBar.setForeground(SUCCESS_COLOR);

        } catch (Exception ex) {
            append(consoleOutput, "ERRO: " + ex.getMessage(), ERROR_COLOR);
            statusBar.setText("❌ Erro");
            statusBar.setForeground(ERROR_COLOR);
        } finally {
            System.setOut(old);
        }

        List<Token> tokens = Scanner.lex(codigo);

        Translator translator = new Translator(tokens);
        String pascal = translator.traduzir();

        translator.salvar(pascal, "output.pas");

        append(consoleOutput, "\n--- Código Pascal ---\n", ACCENT_COLOR);
        append(consoleOutput, pascal, TEXT_COLOR);

        PascalRunner runner = new PascalRunner();
        String resultado = runner.executar("output.pas");

        append(consoleOutput, "\n--- Execução Pascal ---\n", ACCENT_COLOR);
        append(consoleOutput, resultado, TEXT_COLOR);
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