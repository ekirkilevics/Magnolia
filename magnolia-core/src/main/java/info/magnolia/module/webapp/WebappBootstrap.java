/**
 * This file Copyright (c) 2003-2008 Magnolia International
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

import info.magnolia.importexport.Bootstrapper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.apache.commons.lang.StringUtils;

/**
 * TODO : exception handling...
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class WebappBootstrap extends AbstractTask {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebappBootstrap.class);

    public WebappBootstrap() {
        super("Webapp bootstrap", "Bootstraps the webapp contents (files under " + StringUtils.join(Bootstrapper.getBootstrapDirs(), ", ") + ")");
    }

    // TODO exception handling ?
    public void execute(InstallContext installContext) throws TaskExecutionException {
        final String[] bootDirs = Bootstrapper.getBootstrapDirs();
        if (bootDirs.length == 0) {
            throw new IllegalStateException("No boot dir configured !"); //TODO
        }

        // at least one bootstrap directory is configured, trying to initialize repositories
        Bootstrapper.bootstrapRepositories(bootDirs, new Bootstrapper.BootstrapFilter() {
            public boolean accept(String filename) {
                log.debug("Will bootstrap {}", filename);
                return true;
            }
        });
    }
}
