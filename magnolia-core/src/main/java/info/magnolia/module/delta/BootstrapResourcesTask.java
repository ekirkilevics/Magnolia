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

import info.magnolia.cms.beans.config.BootstrapFilesComparator;
import info.magnolia.cms.util.BootstrapUtil;
import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.module.InstallContext;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public abstract class BootstrapResourcesTask extends AbstractTask {

    private boolean backup;
    
    public BootstrapResourcesTask(String name, String description) {
        this(name, description, false);
    }
    
    public BootstrapResourcesTask(String name, String description, boolean backup) {
        super(name, description);
        
        setBackup(backup);
    }

    // TODO : check if nodes were already there
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        try {
            final String[] resourcesToBootstrap = getResourcesToBootstrap(installContext);
            final SortedSet files = new TreeSet(new BootstrapFilesComparator());
            files.addAll(Arrays.asList(resourcesToBootstrap));
            
            for (Iterator iter = files.iterator(); iter.hasNext();) {
                final String resourceName = (String) iter.next();
                final String repositoryName = BootstrapUtil.determineRepository(resourceName);
                final String basePath = BootstrapUtil.determineBasePath(resourceName);
                final InputStream resource = BootstrapResourcesTask.class.getResourceAsStream(resourceName);

                BootstrapUtil.bootstrap(repositoryName, basePath, resource, resourceName, false);
            }
            
        } catch (Exception e) {
            throw new TaskExecutionException("Could not bootstrap: " + e.getMessage().toString(), e);
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

    public boolean isBackup() {
        return backup;
    }
    
    public void setBackup(boolean backup) {
        this.backup = backup;
    }
}
