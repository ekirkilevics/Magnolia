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
package info.magnolia.templating.components;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.templating.components.AbstractContentComponent;

import java.io.IOException;

import javax.jcr.Node;

import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractContentComponentTest extends AbstractComponentTestCase {
    @Test
    public void testGetTargetContent() throws Exception {
        final RenderingContext aggregationState = mock(RenderingContext.class);
        when(aggregationState.getMainContent()).thenReturn(getHM().getNode("/foo/bar"));

        final AbstractContentComponent compo = new DummyComponent(null, aggregationState);
        final Node expectedNode = getHM().getNode("/foo/bar/paragraphs/1");

        when(aggregationState.getCurrentContent()).thenReturn(expectedNode);

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

    private static class DummyComponent extends AbstractContentComponent {
        public DummyComponent(ServerConfiguration serverConfiguration, RenderingContext renderingContext) {
            super(serverConfiguration, renderingContext);
        }

        @Override
        protected void doRender(Appendable out) throws IOException {
            out.append("hello world");
        }
    }
}
