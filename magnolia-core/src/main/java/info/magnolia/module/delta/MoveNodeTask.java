/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MoveNodeTask extends AbstractRepositoryTask {

    private String workspaceName;
    private String src;
    private String dest;
    private boolean overwrite;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MoveNodeTask.class);

    public MoveNodeTask(String name, String description, String workspaceName, String src, String dest, boolean overwrite) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.src = src;
        this.dest = dest;
        this.overwrite = overwrite;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager(workspaceName);
        if(hm.isExist(dest)){
            if(overwrite){
                hm.delete(dest);
            }
            else{
                installContext.error("can't move " + src + " to " + dest + " because the node already exists", new Throwable());
                return;
            }
        }
        // FIXME we should not use the jcr session
        hm.getWorkspace().getSession().move(src, dest);
    }

}
