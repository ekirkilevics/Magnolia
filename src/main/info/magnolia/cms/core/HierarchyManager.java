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


import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.beans.config.ItemType;
import info.magnolia.cms.security.Authenticator;

import javax.jcr.*;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;



/**
 * User: sameercharles
 * Date: Sept 23, 2004
 * Time: 1:42:48 PM
 * @author Sameer Charles
 * @version 2.0
 */


public class HierarchyManager {


    private static Logger log = Logger.getLogger(HierarchyManager.class);

    private Node startPage;
    private Workspace workSpace;
    private HttpServletRequest request;
    private String userID;





    /**
     * constructor
     *
     */
    public HierarchyManager() {
        this.userID = "anonymous";
    }



    public HierarchyManager(String userID) {
        this.userID = userID;
    }


    /**
     * constructor
     *
     */
    public HierarchyManager(HttpServletRequest request) {
        this.request = request;
        this.userID = Authenticator.getUserId(this.request);
    }


    /**
     * sets start page of the current working repository
     *
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     * @deprecated instead use init(Node rootNode)
     * @see HierarchyManager#init(javax.jcr.Node)
     */
    public void setStartPage(Node rootNode)
            throws PathNotFoundException,RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }



    /**
     * initialize hierarchy manager and sets default workspace
     *
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void init(Node rootNode)
            throws PathNotFoundException,RepositoryException {
        this.startPage = rootNode;
        this.workSpace = this.startPage.getSession().getWorkspace();
    }




    /**
     * <p>creates a new content page</p>
     *
     * @param path parent handle under which new page has to be created
     * @param label page name to be created
     * @return Content newly created hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content createPage(String path, String label)
            throws PathNotFoundException,RepositoryException {
        Content newPage = (new Content(this.startPage, this.getNodePath(path,label), true));
        this.setMetaData(newPage.getMetaData());
        return newPage;
    }



    /**
     * <p>creates a new content page</p>
     *
     * @param path parent handle under which new page has to be created
     * @param label page name to be created
     * @param template tamplate name to be assigned to the newly created page
     * @return Content newly created hierarchy node
     * @throws PathNotFoundException
     * @throws RepositoryException
     * @deprecated
     * @see HierarchyManager#createPage(java.lang.String, java.lang.String, java.lang.String)
     */
    public Content createPage(String path, String label, String template, boolean adjustName)
            throws PathNotFoundException,RepositoryException {
        try {
            if (Template.getInfo(template) == null) return null;
        } catch (Exception e) {throw new RepositoryException(); }
        try {
            Content newPage = new Content(this.startPage, this.getNodePath(path,label), true);
            setMetaData(newPage.getMetaData(),template);
            return newPage;
        } catch (ItemExistsException ee) {return this.getPage(this.getNodePath(path,label)); }
    }



    /**
     * <p>creates a new content page</p>
     *
     * @param path parent handle under which new page has to be created
     * @param label page name to be created
     * @param template tamplate name to be assigned to the newly created page
     * @return Content newly created hierarchy node
     * @throws PathNotFoundException
     * @throws RepositoryException
     */
    public Content createPage(String path, String label, String template)
            throws PathNotFoundException,RepositoryException {
        try {
            if (Template.getInfo(template) == null) return null;
        } catch (Exception e) {throw new RepositoryException(); }
        try {
            Content newPage = new Content(this.startPage, this.getNodePath(path,label), true);
            setMetaData(newPage.getMetaData(),template);
            return newPage;
        } catch (ItemExistsException ee) {return this.getPage(this.getNodePath(path,label)); }
    }



    /**
     *
     * */
    public Content createContent(String path, String label, String contentType)
            throws PathNotFoundException,RepositoryException {
        try {
            Content newPage = new Content(this.startPage, this.getNodePath(path,label), contentType);
            setMetaData(newPage.getMetaData());
            return newPage;
        } catch (ItemExistsException ee) {return this.getContent(this.getNodePath(path,label)); }
    }




    private String getNodePath(String path, String label) {
        if (path == null || (path.equals("")) || (path.equals("/")))
            return label;
        return getNodePath(path+"/"+label);
    }


    private String getNodePath(String path) {
        if (path.startsWith("/"))
            path = path.replaceFirst("/","");
        return path;
    }



    /**
     * <p>Helper method to set page properties, create page calls this method.
     * you could call this method anytime to create working page properties</p>
     */
    public void setMetaData (MetaData md, String template) {
        md.setTemplate(template);
        setMetaData(md);
    }


    /**
     * <p>Helper method to set page properties, create page calls this method.
     * you could call this method anytime to create working page properties</p>
     */
    public void setMetaData (MetaData md) {
        md.setCreationDate();
        md.setModificationDate();
        md.setAuthorId(this.userID);
        md.setTitle("");
		md.setSequencePosition();
    }



   /**
     * <p>Helper method to set page properties, get page calls this method.
     * you could call this method anytime to update working page properties</p>
     * :FIXME - author info
     */
    public void updateMetaData (MetaData md) {
        md.setModificationDate();
        md.setAuthorId(this.userID);
    }



    /**
     * <p>returns the page specified by path in the parameter</p>
     *
     * @param path handle of the page to be initialized
     * @return Content hierarchy node
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getPage(String path)
            throws PathNotFoundException,RepositoryException {
        return this.getContent(path);
    }



    /**
     * <p>get content object of the requested URI</p>
     *
     * @param path of the content to be initialized
     * @return Content
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getContent(String path)
            throws PathNotFoundException,RepositoryException {
        if (path.equals("/")) // todo remove this.. caller should take care of this
            return this.getRootPage();
        return (new Content(this.startPage, getNodePath(path)));
    }



    /**
     * <p>get content node object of the requested URI</p>
     *
     * @param path of the content (container / containerlist) to be initialized
     * @return ContentNode
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public ContentNode getContentNode(String path)
            throws PathNotFoundException,RepositoryException {
        return (new ContentNode(this.startPage, getNodePath(path)));
    }



    /**
     * <p>get NodeData object of the requested URI</p>
     *
     * @param path of the atom to be initialized
     * @return NodeData
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public NodeData getNodeData(String path)
            throws PathNotFoundException,RepositoryException {
        return (new NodeData(this.startPage, getNodePath(path)));
    }



     /**
     * <p>returns the first page with a given template name that is found in tree that
      * starts from the page given py the path (including this page)</p>
     *
     * @param path handle of the page from where the search should start
     * @param templateName template name to search for
     * @return first Content hierarchy node that has the specified template name assigned
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public Content getPage(String path, String templateName)
             throws PathNotFoundException,RepositoryException {
        Content page = getPage(path);
        if (page.getTemplate().equals(templateName)) return page;
        Content pageToBeFound = null;
        try {

            if (page.hasChildren()) {
                Collection children = page.getChildren(ItemType.NT_CONTENT,ContentHandler.SORT_BY_NAME);
                Iterator iterator = children.iterator();
                while (iterator.hasNext()) {
                    Content child = (Content) iterator.next();
                    if (child.getTemplate().equals(templateName)) {
                        return child;
                    }
                    if (child.hasChildren()) pageToBeFound = getPage(child.getHandle(),templateName);
                    if (pageToBeFound != null) return pageToBeFound;
                }
            }

        }
        catch (Exception e) {
            log.error("Failed to get - "+path);
            log.error(e.getMessage(), e);
        }
        return pageToBeFound;
    }



    /**
     * <p>removes content page</p>
     *
     * @param path handle od the page to be initialized
     * @throws javax.jcr.PathNotFoundException
     * @throws javax.jcr.RepositoryException
     */
    public void deletePage(String path)
            throws PathNotFoundException,RepositoryException {
        this.startPage.remove(getNodePath(path));
    }



    /**
     *
     * @return startPage of the current working repository-workspace
     */
    public Content getRootPage() {
        return (new Content(this.startPage));
    }



    /**
     * <p>checks if the requested resource is a page (hierarchy Node)</p>
     *
     * @param path of the requested content
     * @return boolean true is the requested content is a Hierarchy Node
     */
    public boolean isPage(String path) {
        try {
            Node n = this.startPage.getNode(getNodePath(path));
            return (n.isNodeType(ItemType.getSystemName(ItemType.NT_CONTENT)));
        } catch (RepositoryException re ) { }

        return false;
    }



    /**
     * <p>
     * check is either the node or property exists with the specified path
     *
     * </p>
     *
     * @param path
     * */
    public boolean isExist(String path) {
        boolean isExist = false;
        try {
            isExist = this.startPage.hasNode(getNodePath(path));
            if (!isExist) {
                isExist = this.startPage.hasProperty(getNodePath(path));
            }
        } catch (RepositoryException re) {
        }

        return isExist;
    }



    /**
     * <p>checks if the requested resource is a ContentNode</p>
     *
     * @param path of the requested node
     * @return boolean true is the requested content is a Node
     */
    public boolean isContentNode(String path) {
        try {
            Node n = this.startPage.getNode(getNodePath(path));
            return (n.isNodeType(ItemType.getSystemName(ItemType.NT_CONTENTNODE)));
        } catch (RepositoryException re ) {
            log.error(re.getMessage(), re);
        }

        return false;
    }



    /**
     * <p>checks if the requested resource is an NodeData (Property)</p>
     *
     * @param path of the requested NodeData
     * @return boolean true is the requested content is an NodeData
     */
    public boolean isNodeData(String path) {
        try {
            Node n = this.startPage.getNode(getNodePath(path));
            return (n.isNodeType(ItemType.getSystemName(ItemType.NT_NODEDATA)));
        } catch (RepositoryException e) {}

        return false;
    }



    /**
     * <p>
     * gets currently used workspace for this hierarchy manager
     * </p>
     * */
    public Workspace getWorkspace() {
        return this.workSpace;
    }



    /**
      *
      * <p>move content to the specified location</p>
      *
      * @param source source document path
      * @param destination where the node has to be moved
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      */
     public void moveTo(String source, String destination)
            throws PathNotFoundException, RepositoryException {
        this.workSpace.move(source, destination);
     }



    /**
      *
      * <p>copy content to the specified location</p>
      *
      * @param source source document path
      * @param destination where the node has to be copied
      * @throws javax.jcr.PathNotFoundException
      * @throws javax.jcr.RepositoryException
      */
     public void copyTo(String source, String destination)
            throws PathNotFoundException, RepositoryException {
        this.workSpace.copy(source, destination);
     }


    /**
     * <p>
     * Persists all changes to the repository if valiation succeds
     * </p>
     * @throws RepositoryException
     * */
    public void save() throws RepositoryException {
        try {
            this.startPage.save();
        } catch (RepositoryException re) {
            log.error(re.getMessage(), re);
            throw re;
        }
    }



    public void refresh() {
        try {
            this.startPage.refresh(true);
        } catch (RepositoryException re) {
            log.error("Failed to refresh");
            log.error(re.getMessage(), re);
        }

    }



}
