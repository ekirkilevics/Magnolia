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

import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.InstallContext;

import java.io.IOException;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class BootstrapResourcesTask extends AbstractTask {

    public BootstrapResourcesTask(String name, String description) {
        super(name, description);
    }

    // TODO : check if nodes were already there
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        try {
            final String[] resourcesToBootstrap = getResourcesToBootstrap(installContext);
            ModuleUtil.bootstrap(resourcesToBootstrap, false);
        } catch (IOException e) {
            throw new TaskExecutionException("Could not bootstrap: " + e.getMessage());
        } catch (RegisterException e) {
            throw new TaskExecutionException("Could not bootstrap: " + e.getMessage());
        }
    }

    /**
     * Override this method to bootstrap specific resource files.
     */
    protected String[] getResourcesToBootstrap(final InstallContext installContext) {
        String[] resourcesToBootstrap = ClasspathResourcesUtil.findResources(new ClasspathResourcesUtil.Filter() {
            public boolean accept(final String name) {
                return acceptResource(installContext, name);
            }
        });
        return resourcesToBootstrap;

    }

    /**
     * Override this method to filter resources to bootstrap.
     */
    protected boolean acceptResource(final InstallContext installContext, final String resourceName) {
        return false;
    }
}
