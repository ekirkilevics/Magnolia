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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Sameer Charles
 * @version $Revision$ ($Author$)
 */
public class Set extends BaseContentTag {

    public static final String SCOPE_GLOBAL = "global";
    
    public static final String SCOPE_LOCAL = "local";

    public static final String SCOPE_PARAGRAPH = "paragraph";

    public static final String SCOPE_CURRENT = "current";

    public static final String SCOPE_PAGE = "page";

    
    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Set.class);

    private String scope = SCOPE_GLOBAL;
    
    /**
     * Reset the former status after executing the body.
     */
    protected boolean forBodyOnly = false;

    /**
     * If forBodyOnly is true we have to reset the former status
     */
    protected Content previousNode;
    
    /**
     * @deprecated
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * @deprecated
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        Content node = getFirstMatchingNode();

        if(isForBodyOnly()){
            saveCurrentNode();
        }

        setNode(node);
        
        return EVAL_BODY_INCLUDE;
    }

    protected void saveCurrentNode() {
        if(SCOPE_GLOBAL.equals(this.getScope())){
            previousNode = Resource.getGlobalContentNode();
        }
        else if (SCOPE_LOCAL.equals(this.getScope()) || SCOPE_PARAGRAPH.equals(this.getScope())){
            previousNode = Resource.getLocalContentNode();
        }
        else if (SCOPE_CURRENT.equals(this.getScope()) || SCOPE_PAGE.equals(this.getScope())){
            previousNode = Resource.getCurrentActivePage();
        }
    }

    protected void setNode(Content node) {
        if(SCOPE_GLOBAL.equals(this.getScope())){
            Resource.setGlobalContentNode(node);
        }
        else if (SCOPE_LOCAL.equals(this.getScope()) || SCOPE_PARAGRAPH.equals(this.getScope())){
            Resource.setLocalContentNode(node);
        }
        else if (SCOPE_CURRENT.equals(this.getScope()) || SCOPE_PAGE.equals(this.getScope())){
            Resource.setCurrentActivePage(node);
        }
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    public int doEndTag() {
        if(isForBodyOnly()){
            setNode(previousNode);
        }
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.contentNode = null;
        this.contentNodeName = null;
    }
    
    public String getScope() {
        return this.scope;
    }
    
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public boolean isForBodyOnly() {
        return this.forBodyOnly;
    }
    
    public void setForBodyOnly(boolean forBodyOnly) {
        this.forBodyOnly = forBodyOnly;
    }
}
