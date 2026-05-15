import java.io.*;
import java.util.function.Consumer;

public class PascalRunner {

    private Process processo;
    private PrintWriter stdinWriter;

    /**
     * Compila o arquivo Pascal e devolve a saída do compilador.
     * Retorna null em caso de sucesso, ou a mensagem de erro.
     */
    public String compilar(String arquivoPascal) {
        StringBuilder out = new StringBuilder();
        try {
            ProcessBuilder compile = new ProcessBuilder("fpc", arquivoPascal);
            compile.redirectErrorStream(true);
            Process proc = compile.start();

            try (BufferedReader r = new BufferedReader(
                    new InputStreamReader(proc.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) out.append(line).append("\n");
            }
            proc.waitFor();

            // FPC retorna exit code 0 quando compila com sucesso
            if (proc.exitValue() != 0) return out.toString();
            return null; // sucesso

        } catch (Exception e) {
            return "[Erro ao compilar] " + e.getMessage();
        }
    }

    /**
     * Inicia o executável gerado de forma interativa.
     * onOutput é chamado sempre que o processo produz uma linha de saída.
     * O executável no Linux não tem extensão; no Windows tem .exe
     */
    public void iniciarExecucao(String arquivoPascal, Consumer<String> onOutput) {
        String exe = arquivoPascal.replace(".pas", "");
        // Windows: tenta com .exe se o arquivo sem extensão não existir
        File exeFile = new File(exe);
        if (!exeFile.exists()) {
            File exeWin = new File(exe + ".exe");
            if (exeWin.exists()) exe = exe + ".exe";
        }

        try {
            ProcessBuilder run = new ProcessBuilder(exe);
            run.redirectErrorStream(true);
            processo = run.start();

            stdinWriter = new PrintWriter(
                new BufferedWriter(new OutputStreamWriter(processo.getOutputStream())), true);

            // Thread separada para ler stdout sem bloquear a EDT
            final String exePath = exe;
            Thread leitor = new Thread(() -> {
                try (BufferedReader r = new BufferedReader(
                        new InputStreamReader(processo.getInputStream()))) {
                    String line;
                    while ((line = r.readLine()) != null) {
                        onOutput.accept(line + "\n");
                    }
                } catch (IOException e) {
                    // processo encerrou normalmente
                }
                onOutput.accept("\n[Programa encerrado]\n");
            });
            leitor.setDaemon(true);
            leitor.start();

        } catch (Exception e) {
            onOutput.accept("[Erro ao executar] " + e.getMessage() + "\n");
        }
    }

    /** Envia uma linha de texto para o stdin do processo em execução. */
    public void enviarEntrada(String linha) {
        if (stdinWriter != null) {
            stdinWriter.println(linha);
            stdinWriter.flush();
        }
    }

    /** Verifica se o processo ainda está em execução. */
    public boolean emExecucao() {
        return processo != null && processo.isAlive();
    }

    /** Encerra o processo se ainda estiver rodando. */
    public void encerrar() {
        if (processo != null && processo.isAlive()) processo.destroyForcibly();
    }
}
