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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ContentNode;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.Resource;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.TagSupport;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * Exposes a content node to the pagecontext as a Map of nodeData, in order to access the exposed object using jstl.
 * Since JSTL doesn't allow calling a method like <code>Content.getNodeData(String)</code> the <code>Content</code>
 * is wrapped into a <code>NodeMapWrapper</code> which exposes NodeData using a map interface. This tag can be useful
 * in similar situations:
 * 
 * <pre>
 * &lt;cms:setNode var="currentNode"/> 
 * &lt;c:if test="${!empty currentNode.title}">
 *   &lt;c:out value="${currentNode.title}"/>
 * &lt;/c:if> 
 * </pre>
 * 
 * @author Fabrizio Giustina
 * @version $Revision: $ ($Author: $)
 */
public class SetNode extends TagSupport {

    /**
     * Stable serialVersionUID.
     */
    private static final long serialVersionUID = 222L;

    /**
     * Logger.
     */
    protected static Logger log = Logger.getLogger(SetNode.class);

    /**
     * Tag attribute. Name of the content node which will be saved in pagecontext.
     */
    private String contentNodeName;

    /**
     * Tag attribute. Name of the collection holding the content node.
     */
    private String contentNodeCollectionName;

    /**
     * Tag attribute. Variable name: the content node will be added to the pagecontext with this name.
     */
    private String var;

    /**
     * Evaluated content node.
     */
    private Content contentNode;

    /**
     * set the content node name name, e.g. "01"
     * @param name
     */
    public void setContentNodeName(String name) {
        this.contentNodeName = name;
    }

    /**
     * set the name of the collection holding the content node, e.g. "mainColumnParagraphs"
     * @param name
     */
    public void setContentNodeCollectionName(String name) {
        this.contentNodeCollectionName = name;
    }

    /**
     * @param var
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    public int doStartTag() {
        ContentNode local = Resource.getLocalContentNode((HttpServletRequest) pageContext.getRequest());
        Content actpage = Resource.getCurrentActivePage((HttpServletRequest) pageContext.getRequest());
        String contentNodeName = this.contentNodeName;
        String contentNodeCollectionName = this.contentNodeCollectionName;
        if (StringUtils.isNotEmpty(contentNodeName)) {
            // contentNodeName is defined
            try {
                if (StringUtils.isEmpty(contentNodeCollectionName)) {
                    // e.g. <cms:setNode contentNodeName="footer"/>
                    this.contentNode = actpage.getContentNode(contentNodeName);
                }
                else {
                    // e.g. <cms:setNode contentNodeName="01" contentNodeCollectionName="mainPars"/>
                    // e.g. <cms:setNode contentNodeName="footer" contentNodeCollectionName=""/>
                    this.contentNode = actpage
                        .getContentNode(contentNodeCollectionName)
                        .getContentNode(contentNodeName);
                }
            }
            catch (RepositoryException re) {
                log.debug(re.getMessage());
            }
        }
        else {
            if (local == null) {
                // outside collection iterator
                if (StringUtils.isNotEmpty(contentNodeCollectionName)) {
                    // ERROR: no content node assignable because contentNodeName is empty
                    // e.g. <cms:setNode contentNodeCollectionName="mainPars"/>
                }
                else {
                    // e.g. <cms:setNode contentNodeName=""/>
                    // e.g. <cms:setNode contentNodeCollectionName=""/>
                    this.contentNode = actpage;
                }
            }
            else {
                // inside collection iterator
                if (contentNodeName == null && contentNodeCollectionName == null) {
                    // e.g. <cms:setNode />
                    this.contentNode = local;
                }
                else if ((contentNodeName != null && contentNodeName.equals(""))
                    || (contentNodeCollectionName != null && contentNodeCollectionName.equals(""))) {
                    // empty collection name -> use actpage
                    // e.g. <cms:setNode contentNodeCollectionName=""/>
                    this.contentNode = actpage;
                }
            }
        }
        return SKIP_BODY;
    }

    /**
     * Set contentNode in pagecontext and continue evaluating jsp.
     * @return int
     */
    public int doEndTag() {
        pageContext.setAttribute(this.var, new NodeMapWrapper(this.contentNode));
        this.contentNode = null;
        return EVAL_PAGE;
    }

    /**
     * @see javax.servlet.jsp.tagext.Tag#release()
     */
    public void release() {
        this.contentNodeCollectionName = null;
        this.contentNodeName = null;
        this.var = null;
        super.release();
    }

    /**
     * Wrapper for a content Node which exposes a Map interface, used to access its content using jstl.
     * @author fgiust
     * @version $Revision: $ ($Author: $)
     */
    public static class NodeMapWrapper implements Map {

        /**
         * The wrapped Content.
         */
        private Content wrappedNode;

        /**
         * Instantiates a new NodeMapWrapper for the given node.
         * @param node Content node
         */
        public NodeMapWrapper(Content node) {
            wrappedNode = node;
        }

        /**
         * @see java.util.Map#size()
         */
        public int size() {
            // not implemented, only get() is needed
            return 0;
        }

        /**
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            // not implemented, only get() is needed
            return false;
        }

        /**
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            // not implemented, only get() is needed
            return false;
        }

        /**
         * @see java.util.Map#containsValue(java.lang.Object)
         */
        public boolean containsValue(Object value) {
            // not implemented, only get() is needed
            return false;
        }

        /**
         * Shortcut for Content.getNodeData(name).getString() or Content.getNodeData(name).getName().
         * @see java.util.Map#get(java.lang.Object)
         */
        public Object get(Object key) {
            NodeData nodeData;
            try {
                nodeData = this.wrappedNode.getNodeData((String) key);
            }
            catch (RepositoryException e) {
                log.warn(e.getMessage(), e);
                return null;
            }
            Object value;
            int type = nodeData.getType();
            if (type == PropertyType.DATE) {
                value = nodeData.getDate();
            }
            else {
                value = nodeData.getString();
            }
            return value;
        }

        /**
         * @see java.util.Map#put(java.lang.Object, java.lang.Object)
         */
        public Object put(Object arg0, Object arg1) {
            // not implemented, only get() is needed
            return null;
        }

        /**
         * @see java.util.Map#remove(java.lang.Object)
         */
        public Object remove(Object key) {
            // not implemented, only get() is needed
            return null;
        }

        /**
         * @see java.util.Map#putAll(java.util.Map)
         */
        public void putAll(Map t) {
            // not implemented, only get() is needed
        }

        /**
         * @see java.util.Map#clear()
         */
        public void clear() {
            // not implemented, only get() is needed
        }

        /**
         * @see java.util.Map#keySet()
         */
        public Set keySet() {
            // not implemented, only get() is needed
            return null;
        }

        /**
         * @see java.util.Map#values()
         */
        public Collection values() {
            // not implemented, only get() is needed
            return null;
        }

        /**
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            // not implemented, only get() is needed
            return null;
        }
    }
}
