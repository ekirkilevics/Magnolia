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
import java.util.Collection;
import java.util.ArrayList;
import java.util.Iterator;

import jdsl.core.ref.ArraySequence;
import jdsl.core.algo.sorts.HeapSort;
import jdsl.core.api.ObjectIterator;
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
		return this.getChildren(ContentHandler.SORT_BY_SEQUENCE);
    }


    /**
     * <p>gets a Collection containing all child nodes at the current level+1 level</p>
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(String sortCriteria) {
        return this.getChildren(ItemType.MAGNOLIA_PAGE, sortCriteria);
    }



    /**
     * <p>get collection either sub pages or sub containers<br>
     * use:<br>
     * ChildrenCollector.SIMPLE_NODE to get sub containers<br>
     * ChildrenCollector.HIERARCHY_NODE to get sub pages
     * </p>
     *
     * @param collectionType , either hierarchy nodes (sub pages) or simple nodes (child containers)
     * @return Collection of content nodes
     */
    public Collection getChildren(int collectionType) {
        return this.getChildren(collectionType,ContentHandler.SORT_BY_SEQUENCE);
    }



    /**
     * <p>get collection either sub pages or sub containers<br>
     * use:<br>
     * ChildrenCollector.SIMPLE_NODE to get sub containers<br>
     * ChildrenCollector.HIERARCHY_NODE to get sub pages
     * </p>
     *
     * @param collectionType , either hierarchy nodes (sub pages) or simple nodes (child containers)
     * @param sortCriteria which can be either ContentHandler.SORT_BY_SEQUENCE , ContentHandler.SORT_BY_DATE or ContentHandler.SORT_BY_NAME
     * @return Collection of content nodes
     */
    public Collection getChildren(int collectionType, String sortCriteria) {
        Collection children = new ArrayList();
        try {
            switch (collectionType) {
                case ItemType.MAGNOLIA_PAGE:
                    children = this.getChildPages();
                    children = sort(children, sortCriteria);
                    break;
                case ItemType.MAGNOLIA_CONTENT_NODE:
                    children = this.getChildContentNodes();
                    children = sort(children, sortCriteria);
                    break;
                case ItemType.MAGNOLIA_NODE_DATA:
                    children = this.getProperties();
                    break;
                default:
                    log.error("Unsupported collection type - "+collectionType);
                    return children;
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
        return (this.getChildren(null).size() > 0);
    }



    /**
     * @return Boolean, if sub <code>collectionType</code> exists
     */
    public boolean hasChildren(int collectionType) {
        //todo reimplement this
        return (this.getChildren(collectionType).size() > 0);
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
            ArraySequence as = new ArraySequence();
            Iterator it = c.iterator();
            while (it.hasNext()) {
                Content content = (Content)it.next();
                as.insertLast(content);
            }
            HeapSort hs = new HeapSort();
            hs.sort(as,new DateComparator());
            Collection sortedCollection = new ArrayList();
            ObjectIterator oi = as.elements();
            while (oi.hasNext()) {
                sortedCollection.add((Content)oi.nextObject());
            }
            return sortedCollection;
        }
        catch (Exception e) {
            log.error(e.getMessage());
            return c;
        }
    }


	/**
	 * <p>gets a Collection containing all clild nodes at the current level+1 level</p>
	 * @return Collection of content nodes
	 */
	public Collection sortBySequence(Collection c) {
		try {
			if (c==null) return c;
			ArraySequence as = new ArraySequence();
			Iterator it = c.iterator();
			while (it.hasNext()) {
				Content content = (Content)it.next();
				as.insertLast(content);
			}
			HeapSort hs = new HeapSort();
			hs.sort(as,new SequenceComparator());
			Collection sortedCollection = new ArrayList();
			ObjectIterator oi = as.elements();
			while (oi.hasNext()) {
				sortedCollection.add(oi.nextObject());
			}
			return sortedCollection;
		}
		catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
			return c;
		}
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
