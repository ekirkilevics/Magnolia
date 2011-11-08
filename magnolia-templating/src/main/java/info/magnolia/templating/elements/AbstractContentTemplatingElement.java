/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia-cms.com). All rights reserved.
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
 * is available at http://www.magnolia-cms.com/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.templating.elements;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;


/**
 * Abstract base class for elements that operate on a specified piece of content.
 *
 * @version $Id$
 */
public abstract class AbstractContentTemplatingElement extends AbstractTemplatingElement {

    // TODO should also support a JSP ContentMap
    private Node content;
    private String workspace;
    private String nodeIdentifier;
    private String path;
    private Map<String, Object> savedCtxAttributes = new HashMap<String, Object>();

    public AbstractContentTemplatingElement(ServerConfiguration server, RenderingContext renderingContext) {
        super(server, renderingContext);
    }

    protected String getNodePath(Node node) throws RenderException {
        try {
            return node.getSession().getWorkspace().getName() + ":" + node.getPath();
        } catch (RepositoryException e) {
            throw new RenderException("Can't construct node path for node " + node);
        }
    }

    protected Node getTargetContent() throws RenderException {

        // TODO should we default workspace to 'website' ?
        // TODO should we be strict and fail on invalid combinations ?

        // TODO we can safely keep the node around after we've resolved it

        if (content != null) {
            return content;
        }
        if (StringUtils.isNotEmpty(workspace)) {
            if (StringUtils.isNotEmpty(nodeIdentifier)) {
                try {
                    return MgnlContext.getJCRSession(workspace).getNodeByIdentifier(nodeIdentifier);
                } catch (RepositoryException e) {
                    throw new RenderException("Can't read content from workspace [" + workspace + "] with identifier [" + nodeIdentifier + "].");
                }
            }
            if (StringUtils.isNotEmpty(path)) {
                try {
                    return MgnlContext.getJCRSession(workspace).getNode(path);
                } catch (RepositoryException e) {
                    throw new RenderException("Can't read content from workspace [" + workspace + "] with path [" + path + "].");
                }
            }
            throw new IllegalArgumentException("Need to specify either uuid or path in combination with workspace");
        }

        // TODO this default might not be suitable for render and paragraph, why would they render the current content again by default?

        return currentContent();
    }

    public Node getContent() {
        return content;
    }

    public void setContent(Node node) {
        this.content = node;
    }

    public String getWorkspace() {
        return workspace;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public String getNodeIdentifier() {
        return nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Sets attributes in web context under the specified scope. If an attribute already exists its value will be overwritten
     * with the new one and the old value saved for subsequent restore.
     * @param scope one of {@link info.magnolia.context.Context#APPLICATION_SCOPE}
     *  {@link info.magnolia.context.Context#SESSION_SCOPE}
     *  {@link info.magnolia.context.Context#LOCAL_SCOPE}.
     */
    protected void setAttributesInWebContext(final Map<String, Object> attributes, int scope) {
        if(attributes == null){
            return;
        }
        switch(scope) {
            case WebContext.APPLICATION_SCOPE:
            case WebContext.SESSION_SCOPE:
            case WebContext.LOCAL_SCOPE:
                break;
            default:
                throw new IllegalArgumentException("Scope is not valid. Use one of the scopes defined in info.magnolia.context.WebContext");
        }
        final WebContext webContext = MgnlContext.getWebContext();
        for(Entry<String, Object> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            if(webContext.containsKey(key)) {
                savedCtxAttributes.put(key, webContext.get(key));
            }
            webContext.setAttribute(key, entry.getValue(), scope);
        }
    }

   /**
    * Restores the original values of attributes in web context under the specified scope.
    * @param scope one of {@link info.magnolia.context.Context#APPLICATION_SCOPE}
    *  {@link info.magnolia.context.Context#SESSION_SCOPE}
    *  {@link info.magnolia.context.Context#LOCAL_SCOPE}.
    */
    protected void restoreAttributesInWebContext(final Map<String, Object> attributes, int scope) {
        if(attributes == null) {
            return;
        }
        switch(scope) {
            case WebContext.APPLICATION_SCOPE:
            case WebContext.SESSION_SCOPE:
            case WebContext.LOCAL_SCOPE:
                break;
            default:
                throw new IllegalArgumentException("Scope is not valid. Use one of the scopes defined in info.magnolia.context.WebContext");
        }
        final WebContext webContext = MgnlContext.getWebContext();

        for(Entry<String, Object> entry : attributes.entrySet()) {
            final String key = entry.getKey();
            if(webContext.containsKey(key) && savedCtxAttributes.get(key) != null) {
                webContext.setAttribute(key, savedCtxAttributes.get(key), scope);
                continue;
            }
            webContext.removeAttribute(key, scope);
        }
    }
}
