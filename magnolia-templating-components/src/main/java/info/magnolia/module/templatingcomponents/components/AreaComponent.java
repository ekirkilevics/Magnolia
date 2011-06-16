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
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.templating.rendering.RenderException;
import info.magnolia.templating.rendering.RenderingContext;
import info.magnolia.templating.rendering.RenderingEngine;
import info.magnolia.templating.template.AreaDefinition;
import info.magnolia.templating.template.RenderableDefinition;
import info.magnolia.templating.template.TemplateDefinition;
import info.magnolia.templating.template.configured.ConfiguredAreaDefinition;
import info.magnolia.templating.template.configured.ConfiguredParagraphAvailability;

import java.io.IOException;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

/**
 * Outputs an area.
 *
 * @version $Id$
 */
public class AreaComponent extends AbstractContentComponent {

    public static final String CMS_AREA = "cms:area";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_SINGLE = "single";
    public static final String DEFAULT_TYPE = TYPE_LIST;

    private String name;
    private AreaDefinition area;
    private String availableComponents;
    private String type;
    private String dialog;
    private RenderingEngine renderingEngine;

    // TODO implement support for script and placeholderScript
    // private String script;
    // private String placeholderScript;

    public AreaComponent(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
    }

    @Override
    protected void doRender(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();
        out.append(CMS_BEGIN_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        out.append(LESS_THAN).append(CMS_AREA);
        param(out, "content", getNodePath(content));

        // Can already be set - or not. If not, we set it in order to avoid tons of if statements in the beyond code...
        if (area == null) {
            area = resolveAreaDefinition();
        }

        param(out, "name", resolveName());
        param(out, "availableComponents", resolveAvailableComponents());
        param(out, "type", resolveType());
        param(out, "dialog", resolveDialog());
        param(out, "showAddButton", String.valueOf(shouldShowAddButton()));

        out.append(GREATER_THAN).append(LESS_THAN).append(SLASH).append(CMS_AREA).append(GREATER_THAN).append(LINEBREAK);
    }

    protected AreaDefinition resolveAreaDefinition() throws RenderException  {
        if(!StringUtils.isEmpty(name)){
            TemplateDefinition templateDefinition = resolveTemplateDefinition();
            if(templateDefinition.getAreas().containsKey(name)){
                return templateDefinition.getAreas().get(name);
            }
        }
        return new ConfiguredAreaDefinition();
    }

    protected TemplateDefinition resolveTemplateDefinition() throws RenderException {
        final RenderableDefinition renderableDefinition = getRenderingContext().getRenderableDefinition();
        if(renderableDefinition instanceof TemplateDefinition){
            return (TemplateDefinition) renderableDefinition;
        }
        throw new RenderException("Current RenderableDefinition [" + renderableDefinition + "] is not of type TemplateDefinition. Areas cannot be supported");
    }

    @Override
    public void postRender(Appendable out) throws RenderException {
        Node content = currentContent();

        try {
            if (isEnabled() && content.hasNode(resolveName())) {

                if (resolveType().equals(TYPE_LIST)) {
                    Node areaNode = content.getNode(resolveName());
                    NodeIterator nodeIterator = areaNode.getNodes();
                    while (nodeIterator.hasNext()) {
                        Node node = (Node) nodeIterator.next();
                        if (node.getPrimaryNodeType().getName().equals(MgnlNodeType.NT_CONTENTNODE)) {
                            renderingEngine.render(node, out);
                        }
                    }
                } else if (resolveType().equals(TYPE_SINGLE)) {
                    // FIXME delegate to the area's script or use a default script
                    // TODO we should suppress any editbar inside the paragraph rendered here
                    Node paragraphNode = content.getNode(resolveName());
                    renderingEngine.render(paragraphNode, out);
                }
            }

            out.append(CMS_END_CONTENT_COMMENT).append(getNodePath(content)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        }
        catch (Exception e) {
            throw new RenderException("Can't render area " + content, e);
        }
    }

    private boolean isEnabled() {
        return (area != null && (area.getEnabled() == null || area.getEnabled()));
    }

    private String resolveDialog() {
        return dialog != null ? dialog : area != null ? area.getDialog() : null;
    }

    private String resolveType() {
        return type != null ? type : area != null && area.getRenderType() != null ? area.getRenderType() : DEFAULT_TYPE;
    }

    private String resolveName() {
        return name != null ? name : area.getName();
    }

    private boolean shouldShowAddButton() throws RenderException {
        if (resolveType().equals(TYPE_LIST)) {
            return true;
        }
        if (resolveType().equals(TYPE_SINGLE)) {
            try {
                return !currentContent().hasNode(resolveName());
            }
            catch (RepositoryException e) {
                throw new RenderException(e);
            }
        }
        throw new IllegalStateException("Unknown area type [" + type + "]");
    }

    protected String resolveAvailableComponents() {
        if (StringUtils.isNotEmpty(availableComponents)) {
            return availableComponents;
        }
        if (area != null && area.getAvailableParagraphs().size() > 0) {
            Iterator<ConfiguredParagraphAvailability> iterator = area.getAvailableParagraphs().values().iterator();
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

    public AreaDefinition getArea() {
        return area;
    }

    public void setArea(AreaDefinition area) {
        this.area = area;
    }

    public String getAvailableComponents() {
        return availableComponents;
    }

    public void setAvailableComponents(String availableComponents) {
        this.availableComponents = availableComponents;
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
