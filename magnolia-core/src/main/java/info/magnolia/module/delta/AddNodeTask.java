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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author brainbug
 * @version $Id$
 *
 */
public class AddNodeTask extends AbstractRepositoryTask {

    private String workspaceName;
    private String parentPath;
    private String label;
    private String type;

    public AddNodeTask(String name, String description, String workspaceName, String parentPath, String label, String type) {
        super(name, description);
        this.workspaceName = workspaceName;
        this.parentPath = parentPath;
        this.label = label;
        this.type = type;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        final HierarchyManager hm = installContext.getHierarchyManager(this.workspaceName);
        
        hm.createContent(getParentPath(), getLabel(), getType());
    }

    
    /**
     * @return the workspaceName
     */
    public String getWorkspaceName() {
        return workspaceName;
    }

    
    /**
     * @param workspaceName the workspaceName to set
     */
    public void setWorkspaceName(String workspaceName) {
        this.workspaceName = workspaceName;
    }

    
    /**
     * @return the parentPath
     */
    public String getParentPath() {
        return parentPath;
    }

    
    /**
     * @param parentPath the parentPath to set
     */
    public void setParentPath(String nodePath) {
        this.parentPath = nodePath;
    }
    
    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    
    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    
    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    
    /**
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }
}
