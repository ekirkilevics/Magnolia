/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2004 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 * */





package info.magnolia.cms.core;


import info.magnolia.cms.beans.config.Paragraph;
import info.magnolia.cms.beans.config.ItemType;

import javax.jcr.*;
import java.util.Collection;

import org.apache.log4j.Logger;



/**
 * User: sameercharles
 * Date: Apr 15, 2003
 * Time: 11:00:20 AM
 * @author Sameer Charles
 * @version 1.1
 */


public class ContentNode extends Content {


    private static Logger log = Logger.getLogger(ContentNode.class);

    private Node workingNode;
    private String name;
    private Content contentNode;





    /**
     * constructor
     */
    public ContentNode () {

    }



    /**
     * Package private constructor
     *
     * @param workingNode parent <code>Node</code>
     * @param name  of the content node to retrieve
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    ContentNode (Node workingNode, String name) throws PathNotFoundException, RepositoryException {
        this.workingNode = workingNode;
        this.name = name;
        this.contentNode = new Content(this.workingNode, this.name);
        this.node = this.contentNode.node;
    }



    /**
     * constructor use to typecast node to ContentNode
     *
     * @param node current <code>Node</code>
     */
    public ContentNode (Node node) {
        this.node = node;
    }



    /**
     * Package private constructor
     *
     * @param workingNode parent <code>Node</code>
     * @param name to be assigned
     * @param createNew creates a new container
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    ContentNode (Node workingNode, String name, boolean createNew) throws PathNotFoundException, RepositoryException {
        this.workingNode = workingNode;
        this.name = name;
        this.contentNode = new Content(this.workingNode, this.name, false);
        this.node = this.contentNode.node;
    }



    /**
     * <p>gets a Collection containing all clild Atoms at the current level</p>
     * @return Collection of Atoms
     */
    public Collection getChildren() {
        Collection children = getChildren(ItemType.MAGNOLIA_CONTENT_NODE);
        return children;
    }



    /**
     * @return Boolean, if sub node(s) exists
     */
    public boolean hasChildren() {
        try {
            return (this.getChildren().size() > 0);
        } catch (Exception e) {return false;}
    }


    /**
     *
     * */
    public String getTemplate() {
        try {
			String templateName=this.getNodeData("paragraph").getString();
			return Paragraph.getInfo(templateName).getTemplatePath();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }



}
