package info.magnolia.repository.definition;

import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class RepositoryMappingDefinitionReaderTest {

    @Test
    public void testParse() throws Exception {

        InputStream stream = getClass().getResourceAsStream("test-repositories.xml");
        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(stream);


        RepositoryMappingDefinition definition = new RepositoryMappingDefinitionReader().read(document);

        assertNotNull(definition);

        Collection<WorkspaceMappingDefinition> mappings = definition.getWorkspaceMappings();
        assertEquals(3, mappings.size());

        Iterator<WorkspaceMappingDefinition> iterator = mappings.iterator();
        WorkspaceMappingDefinition mapping1 = iterator.next();
        assertEquals("website", mapping1.getLogicalWorkspaceName());
        assertEquals("magnolia", mapping1.getRepositoryName());
        assertEquals("website", mapping1.getWorkspaceName());

        WorkspaceMappingDefinition mapping2 = iterator.next();
        assertEquals("data", mapping2.getLogicalWorkspaceName());
        assertEquals("anotherRepository", mapping2.getRepositoryName());
        assertEquals("physicalName", mapping2.getWorkspaceName());

        WorkspaceMappingDefinition mapping3 = iterator.next();
        assertEquals("config", mapping3.getLogicalWorkspaceName());
        assertEquals("magnolia", mapping3.getRepositoryName());
        assertEquals("config", mapping3.getWorkspaceName());

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
