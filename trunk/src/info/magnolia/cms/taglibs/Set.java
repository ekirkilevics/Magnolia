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


import info.magnolia.cms.util.Resource;
import info.magnolia.cms.core.ContentNode;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.http.HttpServletRequest;
import javax.jcr.RepositoryException;

import org.apache.log4j.Logger;


/**
 * User: sameercharles
 * Date: Apr 28, 2003
 * Time: 11:20:59 AM
 * @author Sameer Charles
 * @version 1.1
 */


public class Set extends TagSupport {



    private static Logger log = Logger.getLogger(Set.class);

    private ContentNode contentNode;
    private String contentNodeName;


   /**
     * <p>starts Edit tag</p>
     *
     * @return int
     */
    public int doStartTag() {
        HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
        Resource.removeGlobalContentNode(req);
        if (this.contentNodeName == null)
            Resource.setGlobalContentNode(req,this.contentNode);
        else {
            try {
                this.contentNode = Resource.getCurrentActivePage(req).getContentNode(this.contentNodeName);
                Resource.setGlobalContentNode(req,this.contentNode);
            } catch (RepositoryException re) {log.error(re.getMessage());}
        }
       return SKIP_BODY;
    }



    /**
     * <p>continue evaluating jsp</p>
     *
     * @return int
     */
    public int doEndTag() {
        return EVAL_PAGE;
    }


	/**
	 * @deprecated
	 */
	public void setContainer(ContentNode contentNode) {
		this.setContentNode(contentNode);
	}


    /**
     *
     * @param contentNode to be set
     */
    public void setContentNode(ContentNode contentNode) {
        this.contentNode = contentNode;
    }


	/**
	 * @deprecated
	 */
	public void setContainerName(String name) {
		this.setContentNodeName(name);
	}


    /**
     *
     * @param name , contentNode name to be set
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }



}
