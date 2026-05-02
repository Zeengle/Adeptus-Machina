import java.io.*;

public class PascalRunner {

    public static String executar(String arquivoPascal) {
        StringBuilder output = new StringBuilder();

        try {
            // =========================
            // 1. COMPILA
            // =========================
            ProcessBuilder compile = new ProcessBuilder("fpc", arquivoPascal);
            compile.redirectErrorStream(true);

            Process procCompile = compile.start();

            BufferedReader readerCompile = new BufferedReader(
                    new InputStreamReader(procCompile.getInputStream())
            );

            String line;
            while ((line = readerCompile.readLine()) != null) {
                output.append(line).append("\n");
            }

            procCompile.waitFor();

            // =========================
            // 2. EXECUTA
            // =========================
            String exe = arquivoPascal.replace(".pas", ".exe");

            ProcessBuilder run = new ProcessBuilder(exe);
            run.redirectErrorStream(true);

            Process procRun = run.start();

            BufferedReader readerRun = new BufferedReader(
                    new InputStreamReader(procRun.getInputStream())
            );

            output.append("\n--- OUTPUT DO PROGRAMA ---\n");

            while ((line = readerRun.readLine()) != null) {
                output.append(line).append("\n");
            }

            procRun.waitFor();

        } catch (Exception e) {
            output.append("\n[ERRO AO EXECUTAR PASCAL]\n");
            output.append(e.getMessage());
        }

        return output.toString();
    }
}