/**
 * This file Copyright (c) 2003-2011 Magnolia International
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
package info.magnolia.module.templating.renderers;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.util.ContentWrapper;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.templating.AbstractRenderer;
import info.magnolia.module.templating.RenderableDefinition;
import info.magnolia.module.templating.RenderingModel;
import info.magnolia.module.templating.engine.DefaultRenderingEngine;
import info.magnolia.module.templating.engine.RenderingEngine;
import info.magnolia.module.templating.paragraphs.JspParagraphRenderer;
import info.magnolia.test.ComponentsTestUtil;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspTemplateRendererTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        super.tearDown();
    }

    public void testExposesNodesAsMaps() throws Exception {
        final WebContext magnoliaCtx = createStrictMock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);
        // the page node is exposed twice, once as "actpage", once as "content"
        final Content page = createStrictMock(Content.class);
        expect(page.getHandle()).andReturn("/myPage").times(2);

        final AggregationState aggState = new AggregationState();
        aggState.setMainContent(page.getJCRNode());
        expect(magnoliaCtx.getAggregationState()).andStubReturn(aggState);

        replay(magnoliaCtx, page);
        final Map templateCtx = new HashMap();
        final JspParagraphRenderer renderer = new JspParagraphRenderer();

        // ugly hack to exexute renderer.setupContext()
        Method setupContextMethod = AbstractRenderer.class.getDeclaredMethod("setupContext", new Class[]{Map.class, Content.class, RenderableDefinition.class, RenderingModel.class, Object.class});
        setupContextMethod.setAccessible(true);
        setupContextMethod.invoke(renderer, new Object[]{templateCtx, page, null, null, null});

        // other tests should verify the other objects !
        assertEquals("Unexpected amount of objects in context", 7, templateCtx.size());
        assertTrue(templateCtx.get("actpage") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("actpage")));

        assertTrue(templateCtx.get("content") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("content")));

        verify(magnoliaCtx, page);
    }

    private Content unwrap(Content c) {
        if (c instanceof ContentWrapper) {
            return unwrap(((ContentWrapper) c).getWrappedContent());
        }
        return c;
    }
}
