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
package info.magnolia.templating.freemarker;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.rendering.template.AreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredAreaDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * $Id$
 */
public class AreaDirectiveTest extends AbstractDirectiveTestCase {
    private ConfiguredTemplateDefinition renderableDef;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        renderableDef = new ConfiguredTemplateDefinition();
        ConfiguredAreaDefinition areaDefinition = new ConfiguredAreaDefinition();
        renderableDef.addArea("stage", areaDefinition);
    }

    @Test
    public void testRenderSimpleBarWithoutAreaNode() throws Exception {
        // GIVEN

        // WHEN
        final String result = renderForTest("[@cms.area name=\"stage\" /]", renderableDef);

        // THEN
        assertEquals(
                "<!-- cms:area content=\"testWorkspace:/foo/bar/paragraphs/1/stage\" name=\"stage\" availableComponents=\"\" type=\"list\" label=\"Stage\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" -->"
                + "\n<!-- /cms:area -->\n", result);
    }

    @Test
    public void testRenderSimpleBar() throws Exception {
        // GIVEN
        Node paragraph1 = getSession().getNode("/foo/bar/paragraphs/1");
        // make sure we have a areaNode...
        paragraph1.addNode("stage", MgnlNodeType.NT_AREA);

        // WHEN
        final String result = renderForTest("[@cms.area name=\"stage\" /]", renderableDef);

        // THEN
        assertEquals(
                "<!-- cms:area content=\"testWorkspace:/foo/bar/paragraphs/1/stage\" name=\"stage\" availableComponents=\"\" type=\"list\" label=\"Stage\" inherit=\"false\" optional=\"false\" showAddButton=\"true\" -->"
                + "\n" + "<!-- /cms:area -->\n", result);
    }

    @Test
    public void testRenderNoBar() throws Exception {
        // GIVEN
        renderableDef.setAreas(new HashMap<String, AreaDefinition>());

        // WHEN
        final String result = renderForTest("[@cms.area name=\"stage\" /]", renderableDef);

        // THEN
        assertEquals("", result);
    }
}
