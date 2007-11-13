/**
 * This file Copyright (c) 2003-2007 Magnolia International
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

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.cms.util.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.PageContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Exposes a content node to the pagecontext as a Map of nodeData, in order to access the exposed object using jstl.
 * Since JSTL doesn't allow calling a method like <code>Content.getNodeData(String)</code> the <code>Content</code>
 * is wrapped into a <code>NodeMapWrapper</code> which exposes NodeData using a map interface. This tag can be useful
 * in similar situations: <p/>
 *
 * <pre>
 * &lt;cms:setNode var="currentNode"/>
 * &lt;c:if test="${!empty currentNode.title}">
 *   &lt;c:out value="${currentNode.title}"/>
 * &lt;/c:if>
 * </pre>
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
     * Setter fot the <code>var</code> tag attribute.
     * @param var variable name: the content node will be added to the pagecontext with this name
     */
    public void setVar(String var) {
        this.var = var;
    }
    
    /**
     * @deprecated use setContentNode(node)
     */
    public void setContent(Content node){
        this.setContentNode(node);
    }

    /**
     * Scope for the declared variable.
     * @param scope Can be <code>page</code>, <code>request</code>, <code>session</code> or
     * <code>application</code><code></code>.
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
     * Set contentNode in pagecontext and continue evaluating jsp.
     * @return int
     */
    public int doEndTag() {
        // Evaluated content node.
        Content contentNode = getFirstMatchingNode();

        // set attribute
        if (contentNode != null) {
            pageContext.setAttribute(this.var, new NodeMapWrapper(contentNode, Resource.getActivePage()), this.scope);
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
        this.contentNode = null;
        this.scope = PageContext.PAGE_SCOPE;
    }

    /**
     * Wrapper for a content Node which exposes a Map interface, used to access its content using jstl.
     * @author fgiust
     * @version $Revision$ ($Author$)
     */
    public class NodeMapWrapper implements Map {

        /**
         * The special "uuid" property, which is exposed by NodeMapWrapper just like any other property
         */
        private static final String UUID_PROPERTY = "uuid";

        /**
         * The special "handle" property, which is exposed by NodeMapWrapper just like any other property.
         */
        private static final String HANDLE_PROPERTY = "handle";

        /**
         * The wrapped Content.
         */
        private Content wrappedNode;

        /**
         * Static active page, needed for links.
         */
        private Content actPage;

        /**
         * Instantiates a new NodeMapWrapper for the given node.
         * @param node Content node
         */
        public NodeMapWrapper(Content node, Content actPage) {
            this.wrappedNode = node;
            this.actPage = actPage;
        }

        /**
         * @see java.util.Map#size()
         */
        public int size() {
            return this.wrappedNode.getNodeDataCollection().size();
        }

        /**
         * @see java.util.Map#isEmpty()
         */
        public boolean isEmpty() {
            return this.wrappedNode.getNodeDataCollection().isEmpty();
        }

        /**
         * @see java.util.Map#containsKey(java.lang.Object)
         */
        public boolean containsKey(Object key) {
            return this.wrappedNode.getNodeData((String) key).isExist() || HANDLE_PROPERTY.equals(key) || UUID_PROPERTY.equals(key);
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
         * @see java.util.Map#get(Object)
         */
        public Object get(Object key) {
            try {
                if(!this.wrappedNode.hasNodeData((String)key)){
                    // the special "handle" property
                    if (HANDLE_PROPERTY.equals(key)) {
                        return wrappedNode.getHandle();
                    }
                    
                    // the uuid
                    if (UUID_PROPERTY.equals(key)) {
                        return wrappedNode.getUUID();
                    }
                }
            }
            catch (RepositoryException e) {
                // should really not happen
                log.error("can't check for node data {" + key + "}", e);
            }

            NodeData nodeData = this.wrappedNode.getNodeData((String) key);
            Object value;
            int type = nodeData.getType();
            if (type == PropertyType.DATE) {
                value = nodeData.getDate();
            }
            else if (type == PropertyType.BINARY) {
                // only file path is supported
                FileProperties props = new FileProperties(this.wrappedNode, (String) key);
                value = props.getProperty(FileProperties.PATH);
            }
            else {
                value = LinkUtil.convertUUIDsToBrowserLinks(nodeData.getString(), this.actPage);
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
            Collection nodeDataCollection = this.wrappedNode.getNodeDataCollection();
            Set keys = new HashSet();
            for (Iterator iter = nodeDataCollection.iterator(); iter.hasNext();) {
                keys.add(((NodeData) iter.next()).getName());
            }

            return keys;
        }

        /**
         * @see java.util.Map#values()
         */
        public Collection values() {
            Collection nodeDataCollection = this.wrappedNode.getNodeDataCollection();
            Collection values = new ArrayList();
            for (Iterator iter = nodeDataCollection.iterator(); iter.hasNext();) {
                values.add(((NodeData) iter.next()).getString());
            }

            return values;
        }

        /**
         * @see java.util.Map#entrySet()
         */
        public Set entrySet() {
            Collection nodeDataCollection = this.wrappedNode.getNodeDataCollection();
            Set keys = new HashSet();
            for (Iterator iter = nodeDataCollection.iterator(); iter.hasNext();) {
                NodeData nd = (NodeData) iter.next();
                final String key = nd.getName();
                final String value = nd.getString();
                keys.add(new Map.Entry() {

                    public Object getKey() {
                        return key;
                    }

                    public Object getValue() {
                        return value;
                    }

                    public Object setValue(Object value) {
                        return value;
                    }
                });
            }

            return keys;
        }
    }

}
