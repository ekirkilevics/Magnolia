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
package info.magnolia.rendering.template.configured;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.TemplateAvailability;
import info.magnolia.rendering.template.TemplateDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link TemplateDefinition} configured in the configuration workspace.
 *
 * @version $Id$
 */
public class ConfiguredTemplateDefinition extends ConfiguredRenderableDefinition implements TemplateDefinition {

    private Boolean visible;
    private String dialog;
    private Map<String, AreaDefinition> areaDefinitions = new HashMap<String, AreaDefinition>();
    private Boolean editable;
    private TemplateAvailability templateAvailability;

    @Override
    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    @Override
    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    @Override
    public String getDialog() {
        return this.dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    @Override
    public Map<String, AreaDefinition> getAreas() {
        return this.areaDefinitions;
    }

    public void setAreas(Map<String, AreaDefinition> areaDefinitions) {
        this.areaDefinitions = areaDefinitions;
    }

    public void addArea(String name, AreaDefinition areaDefinition){
        this.areaDefinitions.put(name, areaDefinition);
    }

    @Override
    public TemplateAvailability getTemplateAvailability() {
        return templateAvailability;
    }

    public void setTemplateAvailability(TemplateAvailability templateAvailability) {
        this.templateAvailability = templateAvailability;
    }
}
