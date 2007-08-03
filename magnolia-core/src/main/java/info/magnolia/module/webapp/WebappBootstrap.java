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
package info.magnolia.module.webapp;

import info.magnolia.cms.beans.config.Bootstrapper;
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
