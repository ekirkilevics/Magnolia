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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

/**
 * A {@link AreaDefinition} configured in the configuration workspace.
 *
 * @version $Id$
 */
public class ConfiguredAreaDefinition extends ConfiguredTemplateDefinition implements AreaDefinition {

    private Map<String, ConfiguredParagraphAvailability> availableParagraphs = new LinkedHashMap<String, ConfiguredParagraphAvailability>();

    private boolean enabled = true;
    private String type;

    private String availableComponentNames;

    @Override
    public Map<String, ConfiguredParagraphAvailability> getAvailableParagraphs() {
        return availableParagraphs;
    }

    public void setAvailableParagraphs(Map<String, ConfiguredParagraphAvailability> availableParagraphs) {
        this.availableParagraphs = availableParagraphs;
    }

    public void addAvailableParagraph(String name, ConfiguredParagraphAvailability configuredParagraphAvailability) {
        this.availableParagraphs.put(name, configuredParagraphAvailability);
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

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConfiguredAreaDefinition)) {
            return false;
        }

        ConfiguredAreaDefinition that = (ConfiguredAreaDefinition) o;

        if (enabled != that.enabled) {
            return false;
        }
        if (availableParagraphs != null ? !availableParagraphs.equals(that.availableParagraphs) : that.availableParagraphs != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = availableParagraphs != null ? availableParagraphs.hashCode() : 0;
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public void setAvailableComponentNames(String availableComponentNames) {
        this.availableComponentNames = availableComponentNames;
    }

    @Override
    public String getAvailableComponentNames() {
        if (!StringUtils.isBlank(this.availableComponentNames)) {
            return this.availableComponentNames;
        }
        Iterator<ConfiguredParagraphAvailability> iterator = getAvailableParagraphs().values().iterator();
        List<String> componentNames = new ArrayList<String>();
        while (iterator.hasNext()) {
            componentNames.add(iterator.next().getName());
        }
        return StringUtils.join(componentNames, ',');
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }
}
