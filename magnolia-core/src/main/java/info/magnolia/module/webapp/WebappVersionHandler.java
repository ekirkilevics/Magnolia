/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.webapp;

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.delta.Task;
import info.magnolia.module.model.Version;
import info.magnolia.objectfactory.Components;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.repository.RepositoryManager;

import javax.jcr.RepositoryException;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * ModuleVersionHandler for the webapp module; bootstraps webapp content only if all workspaces are empty.
 * @see info.magnolia.module.webapp.WebappBootstrap
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebappVersionHandler implements ModuleVersionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebappVersionHandler.class);

    @Override
    public Version getCurrentlyInstalled(InstallContext ctx) {
        try {
            final boolean anyContent = checkIfInitialized();
            log.info("Content was {}found in the repository, will {}bootstrap web-app.", (anyContent ? "" : "not "), (anyContent ? "not " : ""));
            if (anyContent) {
                return Version.UNDEFINED_TO;
            }
            // no content, so we'll execute.
            return null;

        } catch (RepositoryException e) {
            //TODO
            throw new RuntimeException("Couldn't check if repositories were empty: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Delta> getDeltas(InstallContext ctx, Version from) {
        if (from == null) {
            final Version version = ctx.getCurrentModuleDefinition().getVersion();
            return Collections.<Delta>singletonList(new WebappDelta(version));
        } else if (!from.equals(Version.UNDEFINED_TO)) {
            throw new IllegalStateException("This is a dummy module. It should not get updated.");
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Delta getStartupDelta(InstallContext ctx) {
        return DeltaBuilder.startup(ctx.getCurrentModuleDefinition(), Collections.<Task>emptyList());
    }

    /**
     * Check if repositories has any content, exclude mgnlVersion repository.
     */
    private boolean checkIfInitialized() throws AccessDeniedException, RepositoryException {
        RepositoryManager repoManager = Components.getComponent(RepositoryManager.class);
        Collection<String> workspaceNames = repoManager.getWorkspaceNames();
        for (String workspace : workspaceNames) {
            if (!workspace.equals(RepositoryConstants.VERSION_STORE) && repoManager.checkIfInitialized(workspace)) {
                return true;
            }
        }
        return false; 
    }
}
