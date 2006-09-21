package info.magnolia.maven.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;


public class ExecUtil {

    public static int exec(String cmd, Log log) {
        try {
            String line;
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                log.info(line);
            }
            input.close();
            p.waitFor();
            return p.exitValue();
        }
        catch (Exception err) {
            err.printStackTrace();
        }
        return -1;
    }

    public static void execGoal(String goal, Log log) {
        exec("mvn " + goal, log);
    }
}
