/**
 * This file Copyright (c) 2008-2012 Magnolia International
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
package info.magnolia.rendering.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import info.magnolia.rendering.template.configured.ConfiguredRenderableDefinition;
import info.magnolia.test.mock.jcr.MockNode;

import javax.jcr.Node;

import org.junit.Test;

/**
 * @version $Id$
 */
public class RenderingModelImplTest {

    @Test
    public void testGetNode() {
        // GIVEN
        MockNode content = new MockNode();
        RenderingModelImpl<ConfiguredRenderableDefinition> model = new RenderingModelImpl<ConfiguredRenderableDefinition>(content, null,null);

        // WHEN
        Node result = model.getNode();

        // THEN
        assertEquals(content, result);
    }

    @Test
    public void testGetDef() {
        // GIVEN
        ConfiguredRenderableDefinition definition = new ConfiguredRenderableDefinition();
        RenderingModelImpl<ConfiguredRenderableDefinition> model = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, definition,null);

        // WHEN
        ConfiguredRenderableDefinition result = model.getDef();

        // THEN
        assertEquals(definition, result);
    }
    @Test
    public void testGetDefinition() {
        // GIVEN
        ConfiguredRenderableDefinition definition = new ConfiguredRenderableDefinition();
        RenderingModelImpl<ConfiguredRenderableDefinition> model = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, definition,null);

        // WHEN
        ConfiguredRenderableDefinition result = model.getDefinition();

        // THEN
        assertEquals(definition, result);
    }

    @Test
    public void testGetRoot() {
        // GIVEN
        RenderingModelImpl<ConfiguredRenderableDefinition> parent = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, null,null);
        RenderingModelImpl<ConfiguredRenderableDefinition> child = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, null, parent);
        RenderingModelImpl<ConfiguredRenderableDefinition> childOfChild = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, null, child);

        // WHEN
        RenderingModel<?> result = childOfChild.getRoot();

        // THEN
        assertEquals(parent, result);
    }

    @Test
    public void testExecute() {
        // WHEN
        String result = new RenderingModelImpl<ConfiguredRenderableDefinition>(null, null, null).execute();

        // THEN
        assertNull(result);
    }
}
