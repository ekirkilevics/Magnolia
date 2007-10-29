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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * Sets a new value for a property.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class SetPropertyTask extends AbstractRepositoryTask {

    private final String workspaceName;

    private final String nodePath;

    private final String propertyName;

    private final String newValue;

    /**
     * @param workspaceName
     * @param nodePath
     * @param propertyName
     * @param newValue
     */
    public SetPropertyTask(String workspaceName, String nodePath, String propertyName, String newValue) {
        super("Sets the value for " + nodePath + "/" + propertyName + " value", "Sets the value for "
            + nodePath
            + "/"
            + propertyName
            + " value");
        this.workspaceName = workspaceName;
        this.nodePath = nodePath;
        this.propertyName = propertyName;
        this.newValue = newValue;
    }

    /**
     * {@inheritDoc}
     */
    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {

        HierarchyManager hm = installContext.getHierarchyManager(workspaceName);

        Content node = hm.getContent(nodePath);

        NodeData property = NodeDataUtil.getOrCreate(node, propertyName);

        String actualValue = property.getString();
        if (!StringUtils.equals(newValue, StringUtils.trim(actualValue))) {
            property.setValue(newValue);
        }
    }
}
