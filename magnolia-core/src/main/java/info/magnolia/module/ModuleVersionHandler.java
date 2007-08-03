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

import info.magnolia.module.model.Version;

import java.util.List;

/**
 * This class provides Delta's to be applied to install/update/uninstall modules.
 * A module that needs to handle its own install/updates should provide an implementation
 * of this interface.
 *
 * @see AbstractModuleVersionHandler for a convenient super class.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public interface ModuleVersionHandler {

    /**
     * Gets the currently installed version number of this module.
     */
    Version getCurrentlyInstalled(InstallContext ctx);

    /**
     * Returns the deltas to be applied to update from the given Version from
     * to the current one. If from is null, it means the module is being installed,
     * and we should thus return the necessary deltas to <strong>install</strong>
     * it.
     * It is also responsible for updating the current version number of the module,
     * wherever it is stored.
     */
    List getDeltas(InstallContext installContext, Version from);

    // TODO : the two methods can maybe be merged, since they're called sequentially in ModuleManager
}
