/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.maven.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.apache.maven.plugin.logging.Log;

/**
 * Execute maven. Used to bootstrap author and public
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public class ExecUtil {

    public static int exec(String cmd, Log log, File workingDirectory) {
        try {
            String line;
           
            Process p = Runtime.getRuntime().exec(cmd, null, workingDirectory);
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

    public static void execGoal(String goal, Log log, File workingDirectory) {
        exec("mvn " + goal, log, workingDirectory);
    }
}
