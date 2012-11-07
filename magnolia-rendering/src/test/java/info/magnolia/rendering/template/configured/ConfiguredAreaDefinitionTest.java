/**
 * This file Copyright (c) 2011-2012 Magnolia International
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import info.magnolia.rendering.template.ComponentAvailability;

import java.util.Collections;
import java.util.Map;

import org.junit.Test;

/**
 * @version $Id$
 */
public class ConfiguredAreaDefinitionTest {

    @Test
    public void testAvailableComponentsIsInitiallyEmpty() {
        // GIVEN
        ConfiguredAreaDefinition def = new ConfiguredAreaDefinition();

        // WHEN
        int componentsSize = def.getAvailableComponents().size();

        // THEN
        assertEquals(0, componentsSize);
    }

    @Test
    public void testAddAvailableComponents() {
        // GIVEN
        final String availableName = "available1";
        ComponentAvailability available = mock(ComponentAvailability.class);
        ConfiguredAreaDefinition def = new ConfiguredAreaDefinition();

        // WHEN
        def.addAvailableComponent(availableName, available);
        ComponentAvailability result = def.getAvailableComponents().get(availableName);

        // THEN
        assertEquals(available, result);
    }

    @Test
    public void testSetAvailableComponents() {
        // GIVEN
        ConfiguredAreaDefinition def = new ConfiguredAreaDefinition();
        Map<String, ComponentAvailability> newMap = Collections.<String, ComponentAvailability> emptyMap();

        // WHEN
        def.setAvailableComponents(newMap);

        // THEN
        assertEquals(newMap, def.getAvailableComponents());
    }

}
