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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.templating.Area;
import info.magnolia.module.templating.Paragraph;
import info.magnolia.module.templating.RenderException;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.objectfactory.Components;

/**
 * Outputs an area.
 *
 * @version $Id$
 */
public class AreaComponent extends AbstractContentComponent {

    public static final String CMS_AREA = "cms:area";
    public static final String TYPE_COLLECTION = "collection";
    public static final String TYPE_SLOT = "slot";
    public static final String DEFAULT_TYPE = TYPE_COLLECTION;

    private String name;
    private Area area;
    /**
     * Comma separated list of paragraphs.
     */
    private String paragraphs;
    private String type;
    private String dialog;

    // TODO implement support for script and placeholderScript
    // private String script;
    // private String placeholderScript;

    public AreaComponent(ServerConfiguration server, AggregationState aggregationState) {
        super(server, aggregationState);
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RepositoryException {
        Node content = getTargetContent();

        out.append(CMS_BEGIN_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        out.append(LESS_THAN).append(CMS_AREA);
        param(out, "content", getNodePath(content));

        // Can already be set - or not. If not, we set it in order to avoid tons of if statements in the beyond code...
        if (area == null) {
            area = new Area();
        }

        param(out, "name", resolveName());
        param(out, "paragraphs", resolveParagraphNames());
        param(out, "type", resolveType());
        param(out, "dialog", resolveDialog());
        param(out, "showAddButton", String.valueOf(shouldShowAddButton()));

        out.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(CMS_AREA).append(GREATER_THAN).append(LINEBREAK);
    }

    @Override
    public void postRender(Appendable out) throws IOException, RepositoryException {
        Node content = currentContent();

        if (isEnabled() && content.hasNode(resolveName())) {

            // TODO IoC
            RenderingEngine renderingEngine = Components.getComponent(RenderingEngine.class);

            // TODO need to get writer some other way
            PrintWriter writer = MgnlContext.getWebContext().getResponse().getWriter();

            if (resolveType().equals(TYPE_COLLECTION)) {
                Node areaNode = content.getNode(resolveName());
                NodeIterator nodeIterator = areaNode.getNodes();
                while (nodeIterator.hasNext()) {
                    Node node = (Node) nodeIterator.next();
                    if (node.getPrimaryNodeType().getName().equals(ItemType.CONTENTNODE.getSystemName())) {
                        renderParagraph(renderingEngine, writer, node);
                    }
                }
            } else if (resolveType().equals(TYPE_SLOT)) {
                // TODO we should suppress any editbar inside the paragraph rendered here
                Node paragraphNode = content.getNode(resolveName());
                renderParagraph(renderingEngine, writer, paragraphNode);
            }
        }

        out.append(CMS_END_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
    }

    private void renderParagraph(RenderingEngine renderingEngine, PrintWriter writer, Node node) throws RepositoryException {
        // TODO RenderingEngine should use Node instead of Content
        Content wrappedContent = MgnlContext.getHierarchyManager(node.getSession().getWorkspace().getName()).getContentByUUID(node.getUUID());
        try {
            renderingEngine.render(wrappedContent, writer);
        } catch (RenderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private boolean isEnabled() {
        return (area != null && (area.getEnabled() == null || area.getEnabled()));
    }

    private String resolveDialog() {
        return dialog != null ? dialog : area != null ? area.getDialog() : null;
    }

    private String resolveType() {
        return type != null ? type : area != null && area.getType() != null ? area.getType() : DEFAULT_TYPE;
    }

    private String resolveName() {
        return name != null ? name : area.getName();
    }

    private boolean shouldShowAddButton() throws RepositoryException {
        if (resolveType().equals(TYPE_COLLECTION)) {
            return true;
        }
        if (resolveType().equals(TYPE_SLOT)) {
            return !currentContent().hasNode(resolveName());
        }
        throw new IllegalStateException("Unknown area type [" + type + "]");
    }

    protected String resolveParagraphNames() {
        if (StringUtils.isNotEmpty(paragraphs)) {
            return paragraphs;
        }
        if (area != null && area.getParagraphs().size() > 0) {
            Iterator<Paragraph> iterator = area.getParagraphs().values().iterator();
            StringBuilder builder = new StringBuilder();
            builder.append(iterator.next().getName());
            while (iterator.hasNext()) {
                builder.append(",").append(iterator.next().getName());
            }
            return builder.toString();
        }
        return "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getParagraphs() {
        return paragraphs;
    }

    public void setParagraphs(String paragraphs) {
        this.paragraphs = paragraphs;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }
}
