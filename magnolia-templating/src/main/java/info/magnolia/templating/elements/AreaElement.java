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

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.exception.RuntimeRepositoryException;
import info.magnolia.jcr.util.ContentMap;
import info.magnolia.jcr.util.NodeUtil;
import info.magnolia.jcr.wrapper.InheritanceNodeWrapper;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.AppendableOnlyOutputProvider;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * Renders an area and outputs a marker that instructs the page editor to place a bar at this location.
 *
 * @version $Id$
 */
public class AreaElement extends AbstractContentTemplatingElement {

    public static final String CMS_AREA = "cms:area";

    public static final String ATTRIBUTE_COMPONENT = "component";
    public static final String ATTRIBUTE_COMPONENTS = "components";

    private final RenderingEngine renderingEngine;

    private Node parentNode;
    private Node areaNode;
    private TemplateDefinition templateDefinition;
    private AreaDefinition areaDefinition;
    private String name;
    private String type;
    private String dialog;
    private String availableComponents;
    private String label;
    private boolean inherit;


    public AreaElement(ServerConfiguration server, RenderingContext renderingContext, RenderingEngine renderingEngine) {
        super(server, renderingContext);
        this.renderingEngine = renderingEngine;
    }

    @Override
    public void begin(Appendable out) throws IOException, RenderException {
        this.parentNode = getTargetContent();

        this.templateDefinition = resolveTemplateDefinition();
        this.areaDefinition = resolveAreaDefinition();

        // set the values based on the area definition if not passed
        this.name = resolveName();
        this.dialog = resolveDialog();
        this.type = resolveType();
        this.label = resolveLabel();
        this.availableComponents = resolveAvailableComponents();

        this.inherit = isInheritanceEnabled();
        this.areaNode = resolveAreaNode();

        // build an adhoc area definition if no area definition can be resolved
        if(areaDefinition == null){
            buildAdHocAreaDefinition();
        }

        if (isAdmin()) {
            MarkupHelper helper = new MarkupHelper(out);
            if(areaNode != null){
                helper.startContent(areaNode);
            }
            helper.openTag(CMS_AREA).attribute("content", getNodePath(parentNode));
            helper.attribute("name", this.name);
            helper.attribute("availableComponents", this.availableComponents);
            helper.attribute("type", this.type);
            helper.attribute("dialog", this.dialog);
            helper.attribute("label", this.label);
            helper.attribute("inherit", String.valueOf(this.inherit));
            helper.attribute("showAddButton", String.valueOf(shouldShowAddButton()));
            helper.closeTag(CMS_AREA);
        }
    }
    //TODO fgrilli: we should probably place autogeneration code all in one place. Currently autogeneration happens here for areas and in DefaultRenderingEngine
    //for other types. The reason we areas autogeneration is not there too is because of missing info on node type for the node to be created.
    private Node createNewAreaNode() {
        Node newAreaNode = null;
        try {
            newAreaNode = NodeUtil.createPath(this.parentNode, this.name, MgnlNodeType.NT_AREA, true);
            NodeUtil.createPath(newAreaNode, MetaData.DEFAULT_META_NODE, MgnlNodeType.NT_METADATA,true);
        } catch (AccessDeniedException e) {
            new RuntimeRepositoryException("An error occurred while trying to create new area " + this.name, e);
        } catch (PathNotFoundException e) {
            new RuntimeRepositoryException("An error occurred while trying to create new area " + this.name, e);
        } catch (RepositoryException e) {
            new RuntimeRepositoryException("An error occurred while trying to create new area " + this.name, e);
        }
        return newAreaNode;
    }

    protected void buildAdHocAreaDefinition() {
        ConfiguredAreaDefinition addHocAreaDefinition = new ConfiguredAreaDefinition();
        addHocAreaDefinition.setName(this.name);
        addHocAreaDefinition.setDialog(this.dialog);
        addHocAreaDefinition.setType(this.type);
        addHocAreaDefinition.setRenderType(this.templateDefinition.getRenderType());
        areaDefinition = addHocAreaDefinition;
    }

    @Override
    public void end(Appendable out) throws RenderException {
        try {
            if (isEnabled()) {
                Map<String, Object> contextObjects = new HashMap<String, Object>();

                if (areaNode != null) {
                    List<ContentMap> components = new ArrayList<ContentMap>();

                    for (Node node : NodeUtil.getNodes(areaNode, MgnlNodeType.NT_COMPONENT)) {
                        components.add(new ContentMap(node));
                    }

                    if(AreaDefinition.TYPE_SINGLE.equals(type)) {
                        if(components.size() > 1) {
                            throw new RenderException("Can't render single area [" + areaNode + "]: expected one component node but found more.");
                        }
                        if(components.size() == 1) {
                            contextObjects.put(ATTRIBUTE_COMPONENT, components.get(0));
                        } else {
                            contextObjects.put(ATTRIBUTE_COMPONENT, null);
                        }
                    } else {
                        contextObjects.put(ATTRIBUTE_COMPONENTS, components);
                    }
                }
                // FIXME we shouldn't manipulate the area definition directly
                // we should use merge with the proxy approach
                if(areaDefinition.getRenderType() == null && areaDefinition instanceof ConfiguredAreaDefinition){
                    ((ConfiguredAreaDefinition)areaDefinition).setRenderType(this.templateDefinition.getRenderType());
                }
                renderingEngine.render(areaNode, areaDefinition, contextObjects, new AppendableOnlyOutputProvider(out));
                // FIXME we shouldn't manipulate the area definition directly
                // we should use merge with the proxy approach
                if(areaDefinition.getI18nBasename() == null && areaDefinition instanceof ConfiguredAreaDefinition){
                    ((ConfiguredAreaDefinition)areaDefinition).setI18nBasename(this.templateDefinition.getI18nBasename());
                }
            }

            if (isAdmin()) {
                MarkupHelper helper = new MarkupHelper(out);
                if(areaNode != null){
                    helper.endContent(areaNode);
                }
            }
        } catch (Exception e) {
            throw new RenderException("Can't render area " + areaNode + " with name " + this.name, e);
        }
    }

    protected Node resolveAreaNode() throws RenderException {
        final Node content = getTargetContent();
        Node area = null;
        try {
            if(content.hasNode(name)){
                area = content.getNode(name);
            } else {
               area = createNewAreaNode();
            }
        }
        catch (RepositoryException e) {
            throw new RenderException("Can't access area node [" + name + "] on [" + content + "]", e);
        }
        if(isInherit()) {
            area = new InheritanceNodeWrapper(area);
        }
        return area;
    }

    protected AreaDefinition resolveAreaDefinition() {
        if (areaDefinition != null) {
            return areaDefinition;
        }

        if (!StringUtils.isEmpty(name)) {
            if (templateDefinition != null && templateDefinition.getAreas().containsKey(name)) {
                return templateDefinition.getAreas().get(name);
            }
        }
        // happens if no area definition is passed or configured
        // an ad-hoc area definition will be created
        return null;
    }

    protected TemplateDefinition resolveTemplateDefinition() throws RenderException {
        final RenderableDefinition renderableDefinition = getRenderingContext().getRenderableDefinition();
        if (renderableDefinition == null || renderableDefinition instanceof TemplateDefinition) {
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

    private String resolveLabel() {
        return label != null ? label : (areaDefinition != null && StringUtils.isNotBlank(areaDefinition.getTitle()) ? areaDefinition.getTitle() : name);
    }

    private boolean isInheritanceEnabled() {
        return areaDefinition != null && areaDefinition.getInheritance() != null && areaDefinition.getInheritance().isEnabled();
    }

    protected String resolveAvailableComponents() {
        if (StringUtils.isNotEmpty(availableComponents)) {
            return availableComponents;
        }
        if (areaDefinition != null && areaDefinition.getAvailableComponents().size() > 0) {
            Iterator<ComponentAvailability> iterator = areaDefinition.getAvailableComponents().values().iterator();
            List<String> componentIds = new ArrayList<String>();
            while (iterator.hasNext()) {
                ComponentAvailability availableComponent = iterator.next();
                if(availableComponent.isEnabled()) {
                    componentIds.add(availableComponent.getId());
                }
            }
            return StringUtils.join(componentIds, ',');
        }
        return "";
    }

    private boolean shouldShowAddButton() throws RenderException {
        if (type.equals(AreaDefinition.TYPE_LIST)) {
            return true;
        }
        if (type.equals(AreaDefinition.TYPE_SINGLE) || type.equals(AreaDefinition.TYPE_NO_COMPONENT)) {
            try {
                return !parentNode.hasNode(name);
            } catch (RepositoryException e) {
                throw new RenderException(e);
            }
        }
        throw new RenderException("Unknown area type [" + type + "]");
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

    public boolean isInherit() {
        return inherit;
    }

    public void setInherit(boolean inherit) {
        this.inherit = inherit;
    }
}
