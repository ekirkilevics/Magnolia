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

import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.Permission;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * @author Sameer Charles
 * @version 2.1
 * @deprecated use info.magnolia.cms.core.Content instead this calss has been deprecated since magnolia 2.1 since jcr
 * allows to define custom nodetypes there is no use for the wrapper classes over content. use
 * info.magnolia.cms.core.Content to bind to any kind of NodeType
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
        this.contentNode = new Content(this.workingNode, this.name, ItemType.NT_CONTENTNODE, manager);
        this.node = this.contentNode.node;
    }

}
