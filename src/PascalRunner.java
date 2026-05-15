import java.io.*;

public class PascalRunner {

    public static String executar(String arquivoPascal, String stdinData) {
        StringBuilder output = new StringBuilder();

        try {
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

            int exitCompile = procCompile.waitFor();
            if (exitCompile != 0) {
                output.append("\n[ERRO: falha na compilação Pascal — verifique o código gerado]");
                return output.toString();
            }

            String exePath = arquivoPascal.replace(".pas", "");
            File exeWin = new File(exePath + ".exe");
            String exe  = exeWin.exists() ? exePath + ".exe" : exePath;

            ProcessBuilder run = new ProcessBuilder(exe);
            run.redirectErrorStream(true);

            Process procRun = run.start();

            try (OutputStream os = procRun.getOutputStream();
                 PrintWriter pw  = new PrintWriter(new OutputStreamWriter(os))) {
                if (stdinData != null && !stdinData.isEmpty()) {
                    pw.print(stdinData);
                }
                pw.flush();
            }

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

    public static String executar(String arquivoPascal) {
        return executar(arquivoPascal, "");
    }
}