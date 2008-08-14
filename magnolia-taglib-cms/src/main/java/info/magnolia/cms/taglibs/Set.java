/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
 *
 *
 * This file is dual-licensed under both the Magnolia
 * Network Agreement and the GNU General Public License.
 * You may elect to use one or the other of these licenses.
 *
 * This file is distributed in the hope that it will be
 * useful, but AS-IS and WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE, TITLE, or NONINFRINGEMENT.
 * Redistribution, except as permitted by whichever of the GPL
 * or MNA you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or
 * modify this file under the terms of the GNU General
 * Public License, Version 3, as published by the Free Software
 * Foundation.  You should have received a copy of the GNU
 * General Public License, Version 3 along with this program;
 * if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * 2. For the Magnolia Network Agreement (MNA), this file
 * and the accompanying materials are made available under the
 * terms of the MNA which accompanies this distribution, and
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Set contentNode in resource.
 * @jsp.tag name="set" body-content="empty"
 *
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
    private boolean forBodyOnly = false;

    /**
     * If forBodyOnly is true we have to reset the former status
     */
    private Content previousNode;
    
    /**
     * @deprecated use the contentNode attribute
     * @jsp.attribute required="false" rtexprvalue="true" type="info.magnolia.cms.core.Content"
     */
    public void setContainer(Content contentNode) {
        this.setContentNode(contentNode);
    }

    /**
     * @deprecated use the contentNodeName attribute
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContainerName(String name) {
        this.setContentNodeName(name);
    }

    /**
     * @jsp.attribute description="nodeDataName is not supported in this tag !" required="false" rtexprvalue="false"
     */
    public void setNodeDataName(String name) {
        throw new UnsupportedOperationException("nodeDataName not supported in this tag");
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
        this.scope = SCOPE_GLOBAL;
        this.forBodyOnly = false;
        this.previousNode = null;
    }
    
    public String getScope() {
        return this.scope;
    }

    /**
     * Attention this is not the jstl scope but the magnolia scope! Values are
     * <ul>
     * <li>local: same as paragraph</li>
     * <li>paragraph: the current paragraph</li>
     * <li>global: a globally use node (default value)</li>
     * <li>page: same as page</li>
     * <li>current: the current page</li>
     * </ul>
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    public boolean isForBodyOnly() {
        return this.forBodyOnly;
    }

    /**
     * If true the node is unset after the end tag. Default is false.
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setForBodyOnly(boolean forBodyOnly) {
        this.forBodyOnly = forBodyOnly;
    }
}
