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
package info.magnolia.templating.components;

import static info.magnolia.cms.core.MgnlNodeType.NT_CONTENTNODE;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
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
    public static final String COMPONENT = "component";
    public static final String COMPONENTS = "components";

    private String name;
    private AreaDefinition areaDefinition;
    private String availableComponents;
    private String type;
    private String dialog;
    private final RenderingEngine renderingEngine;

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
        appendElementStart(out, content, CMS_AREA);
        param(out, "content", getNodePath(content));

        // Can already be set - or not. If not, we set it in order to avoid tons of if statements in the beyond code...
        areaDefinition = resolveAreaDefinition();

        appendParams(areaDefinition, out);

        appendElementEnd(out, CMS_AREA);
    }

    private void appendParams(AreaDefinition area, Appendable out) throws IOException, RenderException {
        System.out.println("this:" + this.name + ", area:" + area.getName());
        param(out, "name", area.getName());
        param(out, "availableComponents", resolveAvailableComponents());
        param(out, "type", resolveType());
        param(out, "dialog", area.getDialog());
        param(out, "showAddButton", String.valueOf(shouldShowAddButton()));
    }

    protected AreaDefinition resolveAreaDefinition() throws RenderException  {
        AreaDefinition clonedArea;
        if (areaDefinition != null) {
            clonedArea = areaDefinition;
        } else {
            if(!StringUtils.isEmpty(name)){
                TemplateDefinition templateDefinition = resolveTemplateDefinition();
                if(templateDefinition.getAreas().containsKey(name)){
                    clonedArea = (AreaDefinition) templateDefinition.getAreas().get(name).clone();
                }
            }
            clonedArea = new ConfiguredAreaDefinition();
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
            clonedArea.setRenderType(resolvedType);
        }
        return clonedArea;
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
        Node areaNode = getTargetContent();

        try {
            if (isEnabled()) {

                Map<String, Object> contextObjects = new HashMap<String, Object>();
                if (resolveType().equals(TYPE_LIST)) {
                    List<ContentMap> components = new ArrayList<ContentMap>();
                    for (Node node : NodeUtil.getNodes(areaNode, NT_CONTENTNODE)) {
                        components.add(new ContentMap(node));
                    }
                    contextObjects.put(COMPONENTS, components);

                } else if (resolveType().equals(TYPE_SINGLE)) {
                    contextObjects.put(COMPONENT, new ContentMap(areaNode));
                }
                if (areaNode != null) {
                    renderingEngine.render(areaNode, areaDefinition, contextObjects, out);
                }
            }

            out.append(CMS_END_CONTENT_COMMENT).append(getNodePath(areaNode)).append(QUOTE).append(XML_END_COMMENT).append(LINEBREAK);
        }
        catch (Exception e) {
            throw new RenderException("Can't render area " + areaNode, e);
        }
    }

    private boolean isEnabled() {
        return (areaDefinition != null && (areaDefinition.getEnabled() == null || areaDefinition.getEnabled()));
    }

    private String resolveDialog() {
        return dialog != null ? dialog : areaDefinition != null ? areaDefinition.getDialog() : null;
    }

    private String resolveType() {
        return type != null ? type : areaDefinition != null && areaDefinition.getRenderType() != null ? areaDefinition.getRenderType() : DEFAULT_TYPE;
    }

    private String resolveName() {
        return name != null ? name : (areaDefinition != null ? areaDefinition.getName() : null);
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
