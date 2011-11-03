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
package info.magnolia.repository.definition;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Test;

/**
 * Test case for RepositoryMappingDefinitionReader.
 */
public class RepositoryMappingDefinitionReaderTest {

    @Test
    public void testParse() throws Exception {
        InputStream stream = getClass().getResourceAsStream("test-repositories.xml");

        RepositoryMappingDefinition definition = new RepositoryMappingDefinitionReader().read(stream);

        assertNotNull(definition);

        Collection<WorkspaceMappingDefinition> mappings = definition.getWorkspaceMappings();
        assertEquals(3, mappings.size());

        Iterator<WorkspaceMappingDefinition> iterator = mappings.iterator();
        WorkspaceMappingDefinition mapping1 = iterator.next();
        assertEquals("website", mapping1.getLogicalWorkspaceName());
        assertEquals("magnolia", mapping1.getRepositoryName());
        assertEquals("website", mapping1.getPhysicalWorkspaceName());

        WorkspaceMappingDefinition mapping2 = iterator.next();
        assertEquals("data", mapping2.getLogicalWorkspaceName());
        assertEquals("anotherRepository", mapping2.getRepositoryName());
        assertEquals("physicalName", mapping2.getPhysicalWorkspaceName());

        WorkspaceMappingDefinition mapping3 = iterator.next();
        assertEquals("config", mapping3.getLogicalWorkspaceName());
        assertEquals("magnolia", mapping3.getRepositoryName());
        assertEquals("config", mapping3.getPhysicalWorkspaceName());

        Collection<RepositoryDefinition> repositories = definition.getRepositories();
        assertEquals(3, repositories.size());

        Iterator<RepositoryDefinition> repositoryDefinitionIterator = repositories.iterator();
        RepositoryDefinition magnolia = repositoryDefinitionIterator.next();
        assertEquals("magnolia", magnolia.getName());
        assertEquals("provider-class", magnolia.getProvider());
        assertTrue(magnolia.isLoadOnStartup());
        assertEquals(2, magnolia.getParameters().size());
        assertEquals("value1", magnolia.getParameters().get("parameter1"));
        assertEquals("value2", magnolia.getParameters().get("parameter2"));
        assertEquals(2, magnolia.getWorkspaces().size());
        assertTrue(magnolia.getWorkspaces().contains("website"));
        assertTrue(magnolia.getWorkspaces().contains("config"));

        RepositoryDefinition anotherRepository = repositoryDefinitionIterator.next();
        assertEquals("anotherRepository", anotherRepository.getName());
        assertEquals("another-provider-class", anotherRepository.getProvider());
        assertFalse(anotherRepository.isLoadOnStartup());
        assertEquals(0, anotherRepository.getParameters().size());
        assertEquals(1, anotherRepository.getWorkspaces().size());
        assertTrue(anotherRepository.getWorkspaces().contains("physicalName"));

        RepositoryDefinition repositoryWithoutWorkspaces = repositoryDefinitionIterator.next();
        assertEquals("repositoryWithoutWorkspaces", repositoryWithoutWorkspaces.getName());
        assertEquals("third-provider-class", repositoryWithoutWorkspaces.getProvider());
        assertFalse(repositoryWithoutWorkspaces.isLoadOnStartup());
        assertEquals(0, repositoryWithoutWorkspaces.getParameters().size());
        assertEquals(1, repositoryWithoutWorkspaces.getWorkspaces().size());
        assertTrue(repositoryWithoutWorkspaces.getWorkspaces().contains("default"));
    }
}
