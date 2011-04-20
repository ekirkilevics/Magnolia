/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.module.templatingcomponents.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.module.templating.Template;
import info.magnolia.module.templating.TemplateManager;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.junit.Test;

/**
 * Tests for AbstractAuthoringUiComponent.
 *
 * @version $Id$
 */
public class AbstractContentComponentTest extends AbstractAuthoringUiComponentTest{
    @Test
    public void testGetTargetContent() throws Exception {
        final MockHierarchyManager hm = MockUtil.createHierarchyManager("/foo/bar/baz/paragraphs/01.text=dummy");

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(hm.getContent("/foo/bar/baz"));

        final AbstractContentComponent compo = new DummyComponent(null, aggregationState);
        final Content content = hm.getContent("/foo/bar/baz/paragraphs/01");
        final Node expectedNode = content.getJCRNode();

        aggregationState.setCurrentContent(content);

        Node node = compo.getTargetContent();
        assertEquals(expectedNode, node);

        compo.setWorkspace("workspace");

        try {
            compo.getTargetContent();
            fail("Expceted IllegalArguementException as workspace is set but not uuid or path");
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }

    public static class TestableTemplateManager extends TemplateManager {
        private final Map<String, Template> templates = new HashMap<String, Template>();

        public void register(Template t) {
            templates.put(t.getName(), t);
        }

        public Template getTemplateDefinition(String key) {
            return templates.get(key);
        }
    }

    private static class DummyComponent extends AbstractContentComponent {
        public DummyComponent(ServerConfiguration serverConfiguration, AggregationState aggregationState) {
            super(serverConfiguration, aggregationState);
        }

        protected void doRender(Appendable out) throws IOException, RepositoryException {
            out.append("hello world");
        }
    }
}
