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



package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;
import info.magnolia.cms.security.AccessDeniedException;

import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Marcel Salathe
 * @version 1.1
 */


public class IfNotEmpty extends BodyTagSupport {


    private static Logger log = Logger.getLogger(IfNotEmpty.class);

    private String nodeDataName = "";
    private String contentNodeName = "";
    private String contentNodeCollectionName = "";
    private ContentNode contentNodeCollection;
    private Content contentNode;
    private NodeData nodeData;
    private String actpage = "false";



        /**
    * <p>start of tag</p>
    *
    * @return int
    */
    public int doStartTag() {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        // in the case where a contentNodeCollectionName is provided
        if (!this.contentNodeCollectionName.equals("")) {
            try {
                this.contentNodeCollection = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeCollectionName);
                }
            catch (RepositoryException re) {}
            if (this.contentNodeCollection == null) return SKIP_BODY;
            if (!this.contentNodeCollection.hasChildren()) return SKIP_BODY;
            return EVAL_BODY_INCLUDE;
        }
        // now the case where no contentNodeCollectionName is provided
        else {
            // if only contentNodeName is provided, it checks if this contentNode exists
            if (!this.contentNodeName.equals("") && this.nodeDataName.equals("")) {
                try {
                    this.contentNode = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeName);
                } catch (RepositoryException re) {log.error(re.getMessage());}
                if (this.contentNode != null) {
                    //contentNode exists, evaluate body
                    return EVAL_BODY_INCLUDE;
                }
            }
            //if both contentNodeName and nodeDataName are set, it checks if that nodeData of that contentNode exitsts and is not empty
            else if (!this.contentNodeName.equals("") && !this.nodeDataName.equals("")) {
                try {
                    this.contentNode = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeName);
                } catch (RepositoryException re) {log.error(re.getMessage());}
                if (this.contentNode != null) {
                    try {
                        this.nodeData = this.contentNode.getNodeData(this.nodeDataName);
                    } catch (AccessDeniedException e) {
                        log.error(e.getMessage());
                    }
                    if ((this.nodeData!=null) && this.nodeData.isExist() && !this.nodeData.getString().equals("")) return EVAL_BODY_INCLUDE;
                }
            }
            // if only nodeDataName is provided, it checks if that nodeData of the current contentNode exists and is not empty
            else if (this.contentNodeName.equals("") && !this.nodeDataName.equals("")) {
                if (this.actpage.equals("true")) {
                    this.contentNode = Resource.getCurrentActivePage((HttpServletRequest)pageContext.getRequest());
                }
                else {
                    this.contentNode = Resource.getLocalContentNode((HttpServletRequest)pageContext.getRequest());
                    if (this.contentNode == null) {
                        this.contentNode = Resource.getGlobalContentNode((HttpServletRequest)pageContext.getRequest());
                        }
                }
                if (this.contentNode != null) {
                    try {
                        this.nodeData = this.contentNode.getNodeData(this.nodeDataName);
                    } catch (AccessDeniedException e) {
                        log.error(e.getMessage());
                    }
                    if ((this.nodeData!=null) && this.nodeData.isExist() && !this.nodeData.getString().equals("")) return EVAL_BODY_INCLUDE;
                }
            }
            // if both contentNodeName and nodeDataName are not provided, it checks if the current contentNode exists
            else {
                this.contentNode = Resource.getLocalContentNode((HttpServletRequest)pageContext.getRequest());
                if (this.contentNode == null) {
                    this.contentNode = Resource.getGlobalContentNode((HttpServletRequest)pageContext.getRequest());
                    }
                if (this.contentNode != null) {
                    return EVAL_BODY_INCLUDE;
                }
            }
        }
        return SKIP_BODY;
    }

	/**
	 * @deprecated
	 */
	public void setAtomName(String name) {
		this.setNodeDataName(name);
	}

    /**
     * @param name , antom name to evaluate
     */
    public void setNodeDataName(String name) {
        this.nodeDataName = name;
    }

	/**
	 * @deprecated
	 */
	public void setContainerName(String name) {
		this.setContentNodeName(name);
	}


    /**
     * @param contentNodeName , contentNodeName to check
     */
    public void setContentNodeName(String contentNodeName) {
        this.contentNodeName = contentNodeName;
    }


	/**
	 * @param name , contentNode collection name
	 * @deprecated
	 */
	public void setContainerListName(String name) {
		this.setContentNodeCollectionName(name);
	}



    /**
     * @param name , contentNodeCollectionName to check
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * <p>set the actpage</p>
     *
     * @param set
     */
    public void setActpage(String set) {
        this.actpage = set;
    }



    public void release() {
        nodeDataName = "";
        contentNodeName = "";
        contentNodeCollectionName = "";
        contentNodeCollection = null;
        contentNode = null;
        nodeData = null;
        actpage = "false";
    }


}

