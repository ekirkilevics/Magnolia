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
import info.magnolia.context.MgnlContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.jsp.PageContext;

/**
 * Exposes a content node to the pagecontext as a Map of nodeData, in order to access the exposed object using JSTL.
 * Since JSTL doesn't allow calling a method like <code>Content.getNodeData(String)</code> the <code>Content</code>
 * is wrapped into a <code>NodeMapWrapper</code> which exposes NodeData using a map interface. This tag can be useful
 * in similar situations: (see @jsp.tag-example)
 *
 * @jsp.tag name="setNode" body-content="empty"
 * @jsp.tag-example
 * <cms:setNode var="currentNode"/>
 * <c:if test="${!empty currentNode.title}">
 *   <c:out value="${currentNode.title}"/>
 * </c:if>
 *
 * @author Fabrizio Giustina
 * @version $Revision$ ($Author$)
 */
public class SetNode extends BaseContentTag {

    /**
     * Logger.
     */
    protected static final Logger log = LoggerFactory.getLogger(SetNode.class);

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Tag attribute. Variable name: the content node will be added to the pagecontext with this name.
     */
    private String var;

    /**
     * Tag attribute. Scope for the declared variable. Can be <code>page</code>, <code>request</code>,
     * <code>session</code> or <code>application</code><code></code>.
     */
    private int scope = PageContext.PAGE_SCOPE;

    /**
     * The content node will be added to the pagecontext with this name.
     * @jsp.attribute required="true" rtexprvalue="true"
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @deprecated use the contentNode attribute
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setContent(Content node){
        this.setContentNode(node);
    }

    /**
     * Scope for the declared variable. Can be "page" (default), "request", "session" or "application".
     * @jsp.attribute required="false" rtexprvalue="true"
     */
    public void setScope(String scope) {
        if ("request".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.REQUEST_SCOPE;
        }
        else if ("session".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.SESSION_SCOPE;
        }
        else if ("application".equalsIgnoreCase(scope)) { //$NON-NLS-1$
            this.scope = PageContext.APPLICATION_SCOPE;
        }
        else {
            // default
            this.scope = PageContext.PAGE_SCOPE;
        }
    }

    /**
     * @jsp.attribute description="nodeDataName is not supported in this tag !" required="false" rtexprvalue="false"
     */
    public void setNodeDataName(String name) {
        throw new UnsupportedOperationException("nodeDataName not supported in this tag");
    }

    /**
     * Set contentNode in pagecontext and continue evaluating jsp.
     * @return int
     */
    public int doEndTag() {
        // Evaluated content node.
        Content contentNode = getFirstMatchingNode();

        // set attribute
        if (contentNode != null) {
            pageContext.setAttribute(this.var, new NodeMapWrapper(contentNode, MgnlContext.getAggregationState().getMainContent()), this.scope);
        }
        else {
            pageContext.removeAttribute(this.var);
        }

        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        super.release();
        this.var = null;
        this.scope = PageContext.PAGE_SCOPE;
    }

    /**
     * Wrapper for a content Node which exposes a Map interface, used to access its content using jstl.
     * @author fgiust
     * @version $Revision$ ($Author$)
     * @deprecated use info.magnolia.cms.util.NodeMapWrapper instead
     */
    public class NodeMapWrapper extends info.magnolia.cms.util.NodeMapWrapper {

        public NodeMapWrapper(Content node, Content actPage) {
            super(node, actPage.getHandle());
        }
    }

}
