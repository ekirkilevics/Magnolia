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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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

    @Test
    public void testEqualsWithSameInstance() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();

        // WHEN
        boolean result = area1.equals(area1);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testEqualsWithSameValues() {
        // GIVEN - both created with default values
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        ConfiguredAreaDefinition area2 = new ConfiguredAreaDefinition();

        // WHEN
        boolean result = area1.equals(area2);

        // THEN
        assertTrue(result);
    }

    @Test
    public void testEqualsWithDifferentClass() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();

        // WHEN
        boolean result = area1.equals("Area51");

        // THEN
        assertFalse(result);
    }

    @Test
    public void testEqualsWithDifferentInEnableds() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        ConfiguredAreaDefinition area2 = new ConfiguredAreaDefinition();
        area1.setEnabled(false);

        // WHEN
        boolean result = area1.equals(area2);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testEqualsWithDifferentTypes() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        ConfiguredAreaDefinition area2 = new ConfiguredAreaDefinition();
        area1.setType("type1");
        area2.setType("anotherType");

        // WHEN
        boolean result = area1.equals(area2);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testEqualsWithDifferentAvailableComps() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        ConfiguredAreaDefinition area2 = new ConfiguredAreaDefinition();
        ComponentAvailability available = mock(ComponentAvailability.class);
        area1.addAvailableComponent("test", available);

        // WHEN
        boolean result = area1.equals(area2);

        // THEN
        assertFalse(result);
    }

    @Test
    public void testHashCodeForDefault() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();

        // WHEN
        int hash = area1.hashCode();

        // THEN
        assertEquals(31, hash);
    }

    @Test
    public void testHashCodeForEnabledFalse() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        area1.setEnabled(false);

        // WHEN
        int hash = area1.hashCode();

        // THEN
        assertEquals(0, hash);
    }

    @Test
    public void testHashCodeForOtherType() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        area1.setType("*");

        // WHEN
        int hash = area1.hashCode();

        // THEN
        assertEquals(73, hash);
    }

    @Test
    public void testHashCodeForDifferentAvailableComps() {
        // GIVEN
        ConfiguredAreaDefinition area1 = new ConfiguredAreaDefinition();
        ComponentAvailability available = mock(ComponentAvailability.class);
        area1.addAvailableComponent("test", available);

        // WHEN
        int hash = area1.hashCode();

        // THEN
        assertEquals(1825920048, hash);
    }
}
