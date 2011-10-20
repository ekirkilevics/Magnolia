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
package info.magnolia.rendering.template.configured;

import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.ComponentAvailability;
import info.magnolia.rendering.template.InheritanceConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link AreaDefinition} configured in the configuration workspace.
 *
 * @version $Id$
 */
public class ConfiguredAreaDefinition extends ConfiguredTemplateDefinition implements AreaDefinition {

    private Map<String, ComponentAvailability> availableComponents = new LinkedHashMap<String, ComponentAvailability>();

    private boolean enabled = true;
    private String type;
    private InheritanceConfiguration inheritance = new ConfiguredInheritance();

    @Override
    public Map<String, ComponentAvailability> getAvailableComponents() {
        return availableComponents;
    }

    public void setAvailableComponents(Map<String, ComponentAvailability> availableComponents) {
        this.availableComponents = availableComponents;
    }

    public void addAvailableComponent(String name, ComponentAvailability configuredComponentAvailability) {
        this.availableComponents.put(name, configuredComponentAvailability);
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public InheritanceConfiguration getInheritance() {
        return inheritance;
    }

    public void setInheritance(InheritanceConfiguration inheritanceConfiguration) {
        this.inheritance = inheritanceConfiguration;
    }


}
