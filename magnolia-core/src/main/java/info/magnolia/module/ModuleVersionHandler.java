/**
 * This file Copyright (c) 2003-2009 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module;

import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.Delta;
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


    // TODO : these two methods can maybe be merged, since they're called sequentially in ModuleManager
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
    List<Delta> getDeltas(InstallContext installContext, Version from);

    /**
     * Returns a list of {@link Task} that needs to be executed always before this module is started. These tasks will
     * be silently applied at startup.
     * @param installContext InstallContext
     * @return List of {@link Task}
     * @deprecated It seems irrelevant to have startup tasks in a VersionHandler. These should probably be moved to ModuleLifecycle.
     */
     Delta getStartupDelta(InstallContext installContext);

}
