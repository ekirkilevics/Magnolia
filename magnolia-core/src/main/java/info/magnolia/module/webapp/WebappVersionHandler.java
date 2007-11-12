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

    public Delta getStartupDelta(InstallContext ctx) {
        return DeltaBuilder.startup(ctx.getCurrentModuleDefinition(), Collections.EMPTY_LIST);
    }
}
