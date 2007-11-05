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
    public static final InstallStatus started = new InstallStatus("started");
    public static final InstallStatus stopped_conditionsNotMet = new InstallStatus("stopped_conditionsNotMet");
    public static final InstallStatus done_restartNeeded = new InstallStatus("done_restartNeeded");
    public static final InstallStatus done_ok = new InstallStatus("done_ok");
    private final String name;

    private InstallStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }
}
