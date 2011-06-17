/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.components;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.MgnlContext;
import info.magnolia.templating.rendering.RenderException;
import info.magnolia.templating.rendering.RenderingContext;

import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract base class for components that operate on a specified piece of content.
 *
 * @version $Id$
 */
public abstract class AbstractContentComponent extends AbstractAuthoringUiComponent {

    public static final String LINEBREAK = "\r\n";
    public static final String GREATER_THAN = ">";
    public static final String LESS_THAN = "<";
    public static final String SLASH = "/";

    public static final String XML_BEGINN_COMMENT = LESS_THAN + "!--" + SPACE;
    public static final String XML_END_COMMENT = SPACE + "--" + GREATER_THAN;

    public static final String CMS_BEGIN_CONTENT = "cms:begin cms:content";
    public static final String CMS_END_CONTENT = "cms:end cms:content";

    public static final String CMS_BEGIN_CONTENT_COMMENT = XML_BEGINN_COMMENT + CMS_BEGIN_CONTENT + EQUALS + QUOTE;

    public static final String CMS_END_CONTENT_COMMENT = XML_BEGINN_COMMENT + CMS_END_CONTENT + EQUALS + QUOTE;

    public static final String FORMAT = SPACE + "format";
    public static final String DIALOG = SPACE + "dialog";

    // TODO should also support a JSP ContentMap
    private Node node;
    private String workspace;
    private String nodeIdentifier;
    private String path;

    public AbstractContentComponent(ServerConfiguration server, RenderingContext renderingContext) {
        super(server, renderingContext);
    }

    protected String getNodePath(Node node) throws RenderException {
        try {
            return node.getSession().getWorkspace().getName() + ":" + node.getPath();
        }
        catch (RepositoryException e) {
            throw new RenderException("Can't constcuct node path for node " + node);
        }
    }

    protected Node getTargetContent() throws RenderException {

        // TODO should we default workspace to 'website' ?
        // TODO should we be strict and fail on invalid combinations ?

        // TODO we can safely keep the node around after we've resolved it

        if (node != null) {
            return node;
        }
        if (StringUtils.isNotEmpty(workspace)) {
            if (StringUtils.isNotEmpty(nodeIdentifier)) {
                try {
                    return MgnlContext.getJCRSession(workspace).getNodeByIdentifier(nodeIdentifier);
                }
                catch (RepositoryException e) {
                    throw new RenderException("Can't read contente from workspace [" + workspace + "] with identifier [" + nodeIdentifier + "].");
                }
            }
            if (StringUtils.isNotEmpty(path)) {
                try {
                    return MgnlContext.getJCRSession(workspace).getNode(path);
                }
                catch (RepositoryException e) {
                    throw new RenderException("Can't read contente from workspace [" + workspace + "] with path [" + path + "].");
                }
            }
            throw new IllegalArgumentException("Need to specify either uuid or path in combination with workspace");
        }

        // TODO this default might not be suitable for render and paragraph, why would they render the current content again by default?

        return currentContent();
    }

    public Node getContent() {
        return node;
    }

    public void setContent(Node node) {
        this.node = node;
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

    protected Appendable appendElementStart(Appendable out, Node content, String tag) throws IOException, RenderException {
        out.append(CMS_BEGIN_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        out.append(LESS_THAN).append(tag);
        return out;
    }

    protected void appendElementEnd(Appendable out, String tag) throws IOException {
        out.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(tag).append(GREATER_THAN).append(LINEBREAK);
    }
}
