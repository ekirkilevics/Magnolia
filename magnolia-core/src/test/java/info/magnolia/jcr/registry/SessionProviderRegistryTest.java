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
package info.magnolia.jcr.registry;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.junit.Test;

/**
 * @version $Id$
 */
public class SessionProviderRegistryTest {
    final static String LOGICAL_WS_NAME = "logicalWSName";
    final static String REPOSITORY_NAME = "repoName";
    final static String PHYSICAL_WS_NAME = "physicalWSName";

    @Test
    public void testGetAllLogicalWorkspaceNames() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();
        registry.addLogical2PhysicalWorkspaceMapping(LOGICAL_WS_NAME, REPOSITORY_NAME, PHYSICAL_WS_NAME);

        // WHEN
        Iterator<String> result = registry.getAllLogicalWorkspaceNames();

        // THEN
        assertEquals(LOGICAL_WS_NAME, result.next());
        assertFalse(result.hasNext());
    }

    @Test
    public void testGetLogicalWorkspaceNameFor() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();
        registry.addLogical2PhysicalWorkspaceMapping(LOGICAL_WS_NAME, REPOSITORY_NAME, PHYSICAL_WS_NAME);

        // WHEN
        String result = registry.getLogicalWorkspaceNameFor(PHYSICAL_WS_NAME);

        // THEN
        assertEquals(LOGICAL_WS_NAME, result);
    }

    @Test
    public void testGetLogicalWorkspaceNameForWithMissingMapping() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();

        // WHEN
        String result = registry.getLogicalWorkspaceNameFor(PHYSICAL_WS_NAME);

        // THEN
        assertEquals(PHYSICAL_WS_NAME, result);
    }


    @Test
    public void testGetRepositoryNameFor() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();
        registry.addLogical2PhysicalWorkspaceMapping(LOGICAL_WS_NAME, REPOSITORY_NAME, PHYSICAL_WS_NAME);

        // WHEN
        String result = registry.getRepositoryNameFor(LOGICAL_WS_NAME);
        // THEN
        assertEquals(REPOSITORY_NAME, result);
    }

    @Test
    public void testGetRepositoryNameForWithMissingMapping() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();

        // WHEN
        String result = registry.getRepositoryNameFor(LOGICAL_WS_NAME);
        // THEN
        assertEquals(LOGICAL_WS_NAME, result);
    }


    @Test
    public void testGetWorkspaceNameFor() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();
        registry.addLogical2PhysicalWorkspaceMapping(LOGICAL_WS_NAME, REPOSITORY_NAME, PHYSICAL_WS_NAME);

        // WHEN
        String result = registry.getWorkspaceNameFor(LOGICAL_WS_NAME);
        // THEN
        assertEquals(PHYSICAL_WS_NAME, result);
    }

    @Test
    public void testGetWorkspaceNameForWithMissingMapping() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();

        // WHEN
        String result = registry.getWorkspaceNameFor(LOGICAL_WS_NAME);
        // THEN
        assertEquals(LOGICAL_WS_NAME, result);
    }


    @Test
    public void testHasMappingFor() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();
        registry.addLogical2PhysicalWorkspaceMapping(LOGICAL_WS_NAME, REPOSITORY_NAME, PHYSICAL_WS_NAME);

        // WHEN
        boolean result = registry.hasMappingFor(LOGICAL_WS_NAME);
        // THEN
        assertTrue(result);
    }

    @Test
    public void testHasMappingForWithNonExistingMapping() {
        // GIVEN
        SessionProviderRegistry registry = new SessionProviderRegistry();

        // WHEN
        boolean result = registry.hasMappingFor(LOGICAL_WS_NAME);
        // THEN
        assertFalse(result);
    }

}
