/**
 * This file Copyright (c) 2003-2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.webapp;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.module.InstallContext;
import info.magnolia.module.ModuleVersionHandler;
import info.magnolia.module.delta.Delta;
import info.magnolia.module.delta.DeltaBuilder;
import info.magnolia.module.model.Version;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebappVersionHandler implements ModuleVersionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebappVersionHandler.class);

    public Version getCurrentlyInstalled(InstallContext ctx) {
        try {
            final boolean anyContent = ContentRepository.checkIfInitialized();
            log.info("Content was {}found in the repository, will {}bootstrap web-app.", (anyContent ? "" : "not "), (anyContent ? "not " : ""));
            if (anyContent) {
                return Version.UNDEFINED_TO;
            } else {
                // no content, so we'll execute.
                return null;
            }

        } catch (RepositoryException e) {
            //TODO
            throw new RuntimeException("Couldn't check if repositories were empty: " + e.getMessage(), e);
        }
    }

    public List getDeltas(InstallContext ctx, Version from) {
        if (from == null) {
            final Version version = ctx.getCurrentModuleDefinition().getVersionDefinition();
            return Collections.singletonList(new WebappDelta(version));
        } else if (!from.equals(Version.UNDEFINED_TO)) {
            throw new IllegalStateException("This is a dummy module. It should not get updated.");
        } else {
            return Collections.EMPTY_LIST;
        }
    }

}
