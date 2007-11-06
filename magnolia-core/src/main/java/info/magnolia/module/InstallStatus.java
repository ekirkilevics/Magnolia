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
 *
 */
package info.magnolia.module;

/**
 * An enum identifying the status/result of the install/update in InstallContext.
 *
 * @see InstallContext#getStatus() 
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class InstallStatus {
    public static final InstallStatus inProgress = new InstallStatus("inProgress");
    public static final InstallStatus stoppedConditionsNotMet = new InstallStatus("stoppedConditionsNotMet");
    public static final InstallStatus installDoneRestartNeeded = new InstallStatus("installDoneRestartNeeded");
    public static final InstallStatus installDone = new InstallStatus("installDone");
    private final String name;

    private InstallStatus(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String toString() {
        return name;
    }
}
