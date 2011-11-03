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
package info.magnolia.rendering.template.assignment;

import info.magnolia.cms.core.MetaData;
import info.magnolia.rendering.template.TemplateDefinition;
import info.magnolia.rendering.template.registry.TemplateDefinitionProvider;
import info.magnolia.rendering.template.registry.TemplateDefinitionRegistry;
import info.magnolia.repository.RepositoryConstants;
import info.magnolia.test.mock.jcr.MockNode;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.junit.Test;

/**
 * @version $Id$
 */
public class MetaDataBasedTemplateDefinitionAssignmentTest {

    @Test
    public void testGetAssignedTemplateDefinition() throws Exception {
        // GIVEN
        final String templateId = "id";
        MockNode node = new MockNode();
        MockNode metaData = (MockNode) node.addNode(MetaData.DEFAULT_META_NODE);
        metaData.setProperty(RepositoryConstants.NAMESPACE_PREFIX + ":" + MetaData.TEMPLATE, templateId);

        TemplateDefinitionRegistry registry = new TemplateDefinitionRegistry(null);
        TemplateDefinition templateDefinition = mock(TemplateDefinition.class);
        TemplateDefinitionProvider provider = mock(TemplateDefinitionProvider.class);
        when(provider.getId()).thenReturn(templateId);
        when(provider.getDefinition()).thenReturn(templateDefinition);

        registry.register(provider);
        MetaDataBasedTemplateDefinitionAssignment assignment = new MetaDataBasedTemplateDefinitionAssignment(registry);

        // WHEN
        TemplateDefinition result = assignment.getAssignedTemplateDefinition(node);

        // THEN
        assertEquals(templateDefinition, result);
    }
}
