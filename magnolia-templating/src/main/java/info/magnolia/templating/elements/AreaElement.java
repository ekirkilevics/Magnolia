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
package info.magnolia.templating.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredParagraphAvailability;
import static info.magnolia.cms.core.MgnlNodeType.*;


/**
 * Renders an area and outputs a marker that instructs the page editor to place a bar at this location.
 *
 * @version $Id$
 */
public class AreaElement extends AbstractContentTemplatingElement {

    public static final String CMS_AREA = "cms:area";

    public static final String ATTRIBUTE_COMPONENT = "component";
    public static final String ATTRIBUTE_COMPONENTS = "components";

    private String name;
    private AreaDefinition areaDefinition;
    private String availableComponents;
    private String type;
    private String dialog;
    private final RenderingEngine renderingEngine;

    // TODO implement support for script and placeholderScript
    // private String script;
    // private String placeholderScript;

    public AreaElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        Node content = getTargetContent();

        areaDefinition = getMergedAreaDefinition();

        System.out.println("this:" + this.name + ", area:" + areaDefinition.getName());

        if (isAdmin()) {
            MarkupHelper helper = new MarkupHelper(out);
            helper.startContent(content);
            helper.openTag(CMS_AREA).attribute("content", getNodePath(content));
            helper.attribute("name", areaDefinition.getName());
            helper.attribute("availableComponents", areaDefinition.getAvailableComponentNames());
            helper.attribute("type", areaDefinition.getType());
            helper.attribute("dialog", areaDefinition.getDialog());
            helper.attribute("showAddButton", String.valueOf(shouldShowAddButton()));
            helper.closeTag(CMS_AREA);
        }
    }

    @Override
    public void end(Appendable out) throws RenderException {
        Node areaNode = getTargetContent();

        try {
            if (isEnabled()) {

                Map<String, Object> contextObjects = new HashMap<String, Object>();
                if (areaDefinition.getType().equals(AreaDefinition.TYPE_LIST)) {
                    List<ContentMap> components = new ArrayList<ContentMap>();
                    for (Node node : NodeUtil.getNodes(areaNode, NT_CONTENTNODE)) {
                        components.add(new ContentMap(node));
                    }
                    contextObjects.put(ATTRIBUTE_COMPONENTS, components);

                } else if (areaDefinition.getType().equals(AreaDefinition.TYPE_SINGLE)) {
                    contextObjects.put(ATTRIBUTE_COMPONENT, new ContentMap(areaNode));
                }
                if (areaNode != null) {
                    renderingEngine.render(areaNode, areaDefinition, contextObjects, out);
                }
            }

            if (isAdmin()) {
                MarkupHelper helper = new MarkupHelper(out);
                helper.endContent(areaNode);
            }
        } catch (Exception e) {
            throw new RenderException("Can't render area " + areaNode, e);
        }
    }

    protected AreaDefinition getMergedAreaDefinition() throws RenderException {

        AreaDefinition clonedArea = null;
        if (areaDefinition != null) {
            clonedArea = areaDefinition;
        } else {
            if (!StringUtils.isEmpty(name)) {
                TemplateDefinition templateDefinition = resolveTemplateDefinition();
                if (templateDefinition.getAreas().containsKey(name)) {
                    clonedArea = (AreaDefinition) templateDefinition.getAreas().get(name).clone();
                }
            }
            if (clonedArea == null) {
                clonedArea = new ConfiguredAreaDefinition();
            }
        }

        // override defaults with settings custom for this instance
        String resolvedName = resolveName();
        System.out.println("resolved name:" + resolvedName);
        if (!StringUtils.isBlank(resolvedName)) {
            clonedArea.setName(resolvedName);
        }

        String resolvedAvailableComponents = resolveAvailableComponents();
        if (!StringUtils.isBlank(resolvedAvailableComponents)) {
            // TODO: see SCRUM-219 for details
            clonedArea.setAvailableComponentNames(resolvedAvailableComponents);
        }
        String resolvedDialog = resolveDialog();
        if (!StringUtils.isBlank(resolvedDialog)) {
            clonedArea.setDialog(resolvedDialog);
        }

        String resolvedType = resolveType();
        if (!StringUtils.isBlank(resolvedType)) {
            clonedArea.setType(resolvedType);
        }
        return clonedArea;
    }

    protected TemplateDefinition resolveTemplateDefinition() throws RenderException {
        final RenderableDefinition renderableDefinition = getRenderingContext().getRenderableDefinition();
        if (renderableDefinition instanceof TemplateDefinition) {
            return (TemplateDefinition) renderableDefinition;
        }
        throw new RenderException("Current RenderableDefinition [" + renderableDefinition + "] is not of type TemplateDefinition. Areas cannot be supported");
    }

    private boolean isEnabled() {
        return areaDefinition != null && areaDefinition.isEnabled();
    }

    private String resolveDialog() {
        return dialog != null ? dialog : areaDefinition != null ? areaDefinition.getDialog() : null;
    }

    private String resolveType() {
        return type != null ? type : areaDefinition != null && areaDefinition.getType() != null ? areaDefinition.getType() : AreaDefinition.DEFAULT_TYPE;
    }

    private String resolveName() {
        return name != null ? name : (areaDefinition != null ? areaDefinition.getName() : null);
    }

    protected String resolveAvailableComponents() {
        if (StringUtils.isNotEmpty(availableComponents)) {
            return availableComponents;
        }
        if (areaDefinition != null && areaDefinition.getAvailableParagraphs().size() > 0) {
            Iterator<ConfiguredParagraphAvailability> iterator = areaDefinition.getAvailableParagraphs().values().iterator();
            List<String> componentNames = new ArrayList<String>();
            while (iterator.hasNext()) {
                componentNames.add(iterator.next().getName());
            }
            return StringUtils.join(componentNames, ',');
        }
        return "";
    }

    private boolean shouldShowAddButton() throws RenderException {
        if (areaDefinition.getType().equals(AreaDefinition.TYPE_LIST)) {
            return true;
        }
        if (areaDefinition.getType().equals(AreaDefinition.TYPE_SINGLE)) {
            try {
                // TODO this should not test using the current users permissions
                return !currentContent().hasNode(areaDefinition.getName());
            } catch (RepositoryException e) {
                throw new RenderException(e);
            }
        }
        throw new RenderException("Unknown area type [" + areaDefinition.getType() + "]");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AreaDefinition getArea() {
        return areaDefinition;
    }

    public void setArea(AreaDefinition area) {
        this.areaDefinition = area;
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
