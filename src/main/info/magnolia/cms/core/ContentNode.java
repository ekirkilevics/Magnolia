/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.cms.core;

import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.core.util.Access;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.1
 */
public class ContentNode extends Content {

    private static Logger log = Logger.getLogger(ContentNode.class);

    private Node workingNode;

    private String name;

    private Content contentNode;

    /**
     * @param workingNode parent <code>Node</code>
     * @param name of the content node to retrieve
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode(Node workingNode, String name, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.workingNode = workingNode;
        this.name = name;
        this.contentNode = new Content(this.workingNode, this.name, manager);
        this.node = this.contentNode.node;
    }

    /**
     * constructor use to typecast node to ContentNode
     * @param node current <code>Node</code>
     */
    public ContentNode(Node node, AccessManager manager) throws RepositoryException, AccessDeniedException {
        Access.isGranted(manager, Path.getAbsolutePath(node.getPath()), Permission.READ);
        this.node = node;
        this.setAccessManager(manager);
    }

    /**
     * Package private constructor
     * @param workingNode parent <code>Node</code>
     * @param name to be assigned
     * @param createNew creates a new container
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public ContentNode(Node workingNode, String name, boolean createNew, AccessManager manager)
        throws PathNotFoundException,
        RepositoryException,
        AccessDeniedException {
        this.workingNode = workingNode;
        this.name = name;
        this.contentNode = new Content(
            this.workingNode,
            this.name,
            ItemType.getSystemName(ItemType.NT_CONTENTNODE),
            manager);
        this.node = this.contentNode.node;
    }

    /**
     * todo this should be a part of meta data, if we change what happens to the existing content where this is stored
     * as NodeData
     */
    public String getTemplate() {
        try {
            String templateName = this.getNodeData("paragraph").getString();
            return Paragraph.getInfo(templateName).getTemplatePath();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return StringUtils.EMPTY;
    }
}
