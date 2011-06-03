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
package info.magnolia.module.templating;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.test.TestMagnoliaConfigurationProperties;
import info.magnolia.test.mock.MockContent;

import javax.jcr.Node;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class AbstractRenderableTest {
    private MockContent dummyContent = new MockContent("");
    private Paragraph dummyDef = new Paragraph();
    private RenderingModel dummyParentModel = new RenderingModelImpl(dummyContent, null, null);

    @Before
    public void setUp() throws Exception {
        SystemProperty.setMagnoliaConfigurationProperties(new TestMagnoliaConfigurationProperties());
    }

    @After
    public void tearDown() throws Exception {
        SystemProperty.clear();
    }

    @Test
    public void testCanInstantiateModel() throws Exception {
        final AbstractRenderable renderable = new AbstractRenderable() {
        };
        renderable.setModelClass((Class< ? extends info.magnolia.templating.model.RenderingModel< ? >>) StandardConstructorModel.class);
        final RenderingModel m = renderable.newModel(dummyContent, dummyDef, dummyParentModel);
        assertNotNull(m);
        assertTrue(m instanceof StandardConstructorModel);
    }

    @Test
    public void testModelNeedSpecificConstructor() {
        final AbstractRenderable renderable = new AbstractRenderable() {
        };
        renderable.setModelClass(NoConstructorModel.class);
        try {
            final RenderingModel m = renderable.newModel(dummyContent, dummyDef, dummyParentModel);
            fail("should have failed");
        } catch (Exception e) {
            assertEquals("A model class must define a constructor with types {interface info.magnolia.cms.core.Content,interface info.magnolia.module.templating.RenderableDefinition,interface info.magnolia.module.templating.RenderingModel}. Can't instantiate class info.magnolia.module.templating.AbstractRenderableTest$NoConstructorModel", e.getMessage());
            assertTrue(e instanceof IllegalArgumentException);
        }

        renderable.setModelClass(WrongConstructorModel.class);
        try {
            final RenderingModel m = renderable.newModel(dummyContent, dummyDef, dummyParentModel);
            fail("should have failed");
        } catch (Exception e) {
            assertEquals("A model class must define a constructor with types {interface info.magnolia.cms.core.Content,interface info.magnolia.module.templating.RenderableDefinition,interface info.magnolia.module.templating.RenderingModel}. " +
                    "Can't instantiate class info.magnolia.module.templating.AbstractRenderableTest$WrongConstructorModel", e.getMessage());
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    public static class NoConstructorModel implements RenderingModel<RenderableDefinition> {
        @Override
        public RenderingModel getParent() {
            return null;
        }

        @Override
        public Node getContent() {
            return null;
        }

        @Override
        public RenderableDefinition getDefinition() {
            return null;
        }

        @Override
        public String execute() {
            return null;
        }
    }

    public static class WrongConstructorModel extends NoConstructorModel {
        public WrongConstructorModel(Content content) {
            // semi random constructor - we might want to support this at some point (non mandatory arguments ?)
        }
    }

    public static class StandardConstructorModel implements RenderingModel {
        public StandardConstructorModel(Content content, RenderableDefinition definition, RenderingModel parent) {

        }

        @Override
        public RenderingModel getParent() {
            return null;
        }

        @Override
        public Node getContent() {
            return null;
        }

        @Override
        public RenderableDefinition getDefinition() {
            return null;
        }

        @Override
        public String execute() {
            return null;
        }
    }
}
