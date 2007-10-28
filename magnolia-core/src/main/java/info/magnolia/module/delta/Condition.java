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
package info.magnolia.module.delta;

import info.magnolia.module.InstallContext;

/**
 * Conditions are checked prior to the installation or update of a module.
 * They check for system configuration which can't be automatically updated,
 * like configuration, dependencies, etc.
 * Modules register their Conditions like their Tasks, for each successive
 * version.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface Condition {
    /**
     * Description of what has changed.
     */
    String getName();

    /**
     * Description what will be checked, and how the user can fix it if it's not
     * set properly.
     */
    String getDescription();

    boolean check(InstallContext installContext);
}
