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


import info.magnolia.cms.util.DateComparator;
import info.magnolia.cms.util.SequenceComparator;
import info.magnolia.cms.beans.config.Server;
import info.magnolia.cms.beans.config.ItemType;

import javax.jcr.*;
import java.util.*;


import org.apache.log4j.Logger;


/**
 * User: sameercharles
 * Date: May 12, 2004
 * Time: 10:28:01 AM
 * @author Sameer Charles
 * @author Marcel Salathe
 *
 * @version 1.5
 *
 */


public class ContentHandler {


    private static Logger log = Logger.getLogger(ContentHandler.class);


    public static final String SORT_BY_DATE = "date";
    public static final String SORT_BY_NAME = "name";
	public static final String SORT_BY_SEQUENCE = "sequence";

    protected Node node;



    /**
     *
     * package private constructor
     *
     */
    ContentHandler() {
    }



    /**
     * <p>bit by bit copy of the current object</p>
     *
     * @return Object cloned object
     * @throws CloneNotSupportedException
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }



    /**
      *
      * <p>get a handle representing path relative to the content repository</p>
      *
      * @return String representing path (handle) of the content
      */
     public String getHandle(){
        try {
            return this.node.getPath();
        } catch (RepositoryException e) {
            log.error("Failed to get handle");
            log.error(e.getMessage(), e);
            return "";
        }
     }



    /**
      *
      * <p>get a handle representing path relative to the content repository with the default extension</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return String representing path (handle) of the content
      */
     public String getHandleWithDefaultExtension() throws PathNotFoundException, RepositoryException {
        return (this.node.getPath()+"."+Server.getDefaultExtension());
     }



    /**
      *
      * <p>get parent content object</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing parent node
      */
     public Content getParent() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getParent()));
     }


    /**
     * <p>
     * gets a Collection containing all child nodes at the current level+1 level<br>
     * </p>
     * @return Collection of content nodes
     */
    public Collection getChildren() {
		return this.getChildren(ItemType.NT_CONTENT);
    }



    /**
     * <p>get collection of specified content type<br>
     * use:<br>
     * ItemType.NT_CONTENT to get sub pages
     * ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties)
     * <b>else</b>
     * YOUR_CUSTOM_TYPE as registered
     * </p>
     *
     * @param contentType
     * @return Collection of content nodes
     * @deprecated
     */
    public Collection getChildren(int contentType) {
        String type = "";
        switch (contentType) {
            case ItemType.MAGNOLIA_PAGE:
                type = ItemType.NT_CONTENT;
                break;
            case ItemType.MAGNOLIA_CONTENT_NODE:
                type = ItemType.NT_CONTENTNODE;
                break;
            case ItemType.MAGNOLIA_NODE_DATA:
                type = ItemType.NT_NODEDATA;
                break;
            default:
                log.error("Un-Supported content type - "+contentType);

        }
        return this.getChildren(type,ContentHandler.SORT_BY_SEQUENCE);
    }



    /**
     * <p>get collection of specified content type<br>
     * use:<br>
     * ItemType.NT_CONTENT to get sub pages
     * ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties)
     * <b>else</b>
     * YOUR_CUSTOM_TYPE as registered
     * </p>
     *
     * @param contentType
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType) {
        return this.getChildren(contentType,ContentHandler.SORT_BY_SEQUENCE);
    }





    /**
     * <p>get collection of specified content type<br>
     * use:<br>
     * ItemType.NT_CONTENT to get sub pages
     * ItemType.NT_CONTENTNODE to get sub content nodes (paragraphs)
     * ItemType.NT_NODEDATA to get node data (properties)
     * <b>else</b>
     * YOUR_CUSTOM_TYPE as registered
     * </p>
     *
     * @param contentType
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(String contentType, String sortCriteria) {
        Collection children = new ArrayList();
        try {
            if (contentType.equalsIgnoreCase(ItemType.NT_CONTENT)) {
                children = this.getChildPages();
                children = sort(children, sortCriteria);
            } else if (contentType.equalsIgnoreCase(ItemType.NT_CONTENTNODE)) {
                children = this.getChildContentNodes();
                children = sort(children, sortCriteria);
            } else if (contentType.equalsIgnoreCase(ItemType.NT_NODEDATA)) {
                children = this.getProperties();
            } else {
                children = this.getChildContent(contentType);
                children = sort(children, sortCriteria);
            }

        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }
        return children;
    }



    private Collection sort(Collection collection, String sortCriteria) {
        if (sortCriteria == null)
            return collection;
        if (sortCriteria.equals(ContentHandler.SORT_BY_DATE))
            return sortByDate(collection);
        else if (sortCriteria.equals(ContentHandler.SORT_BY_SEQUENCE))
            return sortBySequence(collection);
        return collection;
    }


    private Collection getChildPages() throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null)
            return children;
        while (nodeIterator.hasNext()) {
            Node subNode = (Node)nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(ItemType.NT_CONTENT)))
                    children.add(new Content(subNode));
            } catch (PathNotFoundException e) {
            }
        }
        return children;
    }



    private Collection getChildContent(String contentType) throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null)
            return children;
        while (nodeIterator.hasNext()) {
            Node subNode = (Node)nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(contentType)))
                    children.add(new Content(subNode));
            } catch (PathNotFoundException e) {
            }
        }
        return children;
    }


    /**
     *
     * @throws RepositoryException
     * */
    private Collection getChildContentNodes() throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null)
            return children;
        while (nodeIterator.hasNext()) {
            Node subNode = (Node)nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(ItemType.NT_CONTENTNODE)))
                    children.add(new ContentNode(subNode));
            } catch (PathNotFoundException e) {
            }
        }
        return children;
    }






    private Collection getProperties() throws RepositoryException {
        Collection children = new ArrayList();
        NodeIterator nodeIterator = this.node.getNodes();
        if (nodeIterator == null)
            return children;
        while (nodeIterator.hasNext()) {
            Node subNode = (Node)nodeIterator.next();
            try {
                if (subNode.isNodeType(ItemType.getSystemName(ItemType.NT_NODEDATA)))
                    children.add(new NodeData(subNode));
            } catch (PathNotFoundException e) {
            }
        }
        return children;
    }




    /**
      *
      * <p>get absolute parent object starting from the root node</p>
      *
      * @param digree level at which the requested node exist, relative to the ROOT node
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing parent node
      */
     public Content getAncestor(int digree) throws PathNotFoundException, RepositoryException {
        if (digree > this.getLevel())
            throw new PathNotFoundException();
        return (new Content(this.node.getAncestor(digree)));
     }



    /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 0
      */
     public Collection getAncestors() throws PathNotFoundException, RepositoryException {
        ArrayList allAncestors = new ArrayList();
        Content currentNode = new Content(this.node);
        int level = currentNode.getLevel();
        while (level != 0) {
            allAncestors.add(new Content(this.node.getAncestor(--level)));
        }
        return allAncestors;
     }


    /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 0
      */
     public Content getAncestor0() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(0)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 1
      */
     public Content getAncestor1() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(1)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 2
      */
     public Content getAncestor2() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(2)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 3
      */
     public Content getAncestor3() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(3)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 4
      */
     public Content getAncestor4() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(4)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 5
      */
     public Content getAncestor5() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(5)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 6
      */
     public Content getAncestor6() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(6)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 7
      */
     public Content getAncestor7() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(7)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 8
      */
     public Content getAncestor8() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(8)));
     }

     /**
      *
      * <p>Convenience method for taglib</p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return Content representing node on level 9
      */
     public Content getAncestor9() throws PathNotFoundException, RepositoryException {
        return (new Content(this.node.getAncestor(9)));
     }




    /**
      *
      * <p>get node level from the ROOT node
      * : FIXME implement getDepth in javax.jcr
      * </p>
      *
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      * @return level at which current node exist, relative to the ROOT node
      */
     public int getLevel() throws PathNotFoundException, RepositoryException {
        return this.node.getPath().split("/").length - 1;
     }



    /**
     * @return Boolean, if sub node(s) exists
     */
    public boolean hasChildren() {
        //todo reimplement this
        return (this.getChildren(ItemType.NT_CONTENT).size() > 0);
    }



    /**
     * @return Boolean, if sub <code>collectionType</code> exists
     * @deprecated
     */
    public boolean hasChildren(int collectionType) {
        //todo reimplement this
        return (this.getChildren(collectionType).size() > 0);
    }


    /**
     * @return Boolean, if sub <code>collectionType</code> exists
     * @deprecated
     */
    public boolean hasChildren(String contentType) {
        return (this.getChildren(contentType).size() > 0);
    }



    /**
     * Convenicence method to access from JSTL
     *
     *
     * @return Boolean, if sub node(s) exists
     */
    public boolean isHasChildren() {
        return hasChildren();
    }




    /**
     * <p>gets a Collection containing all clild nodes at the current level+1 level</p>
     * @return Collection of content nodes
     */
    public Collection sortByDate(Collection c) {
        try {
            if (c==null) return c;
            Collections.sort((List)c, new DateComparator());
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        return c;
    }


	/**
	 * <p>gets a Collection containing all clild nodes at the current level+1 level</p>
	 * @return Collection of content nodes
	 */
	public Collection sortBySequence(Collection c) {
		try {
			if (c==null) return c;
			Collections.sort((List)c,new SequenceComparator());
		}
		catch (Exception e) {
            log.error(e.getMessage(), e);
		}
        return c;
	}




    /**
     * <p>utility method to get Node object used to create current content object</p>
     *
     * @return Node
     */
    public Node getJCRNode() {
        return this.node;
    }



}
