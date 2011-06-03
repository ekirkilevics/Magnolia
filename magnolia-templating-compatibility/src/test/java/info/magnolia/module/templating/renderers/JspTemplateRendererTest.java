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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.junit.After;
import org.junit.Test;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class JspTemplateRendererTest {

    @After
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        SystemProperty.clear();
    }

    @Test
    public void testExposesNodesAsMaps() throws Exception {
        final WebContext magnoliaCtx = mock(WebContext.class);
        MgnlContext.setInstance(magnoliaCtx);
        ComponentsTestUtil.setImplementation(RenderingEngine.class, DefaultRenderingEngine.class);
        // the page node is exposed twice, once as "actpage", once as "content"
        final Content page = mock(Content.class);
        when(page.getHandle()).thenReturn("/myPage");

        final AggregationState aggState = new AggregationState();
        final Node jcrPage = mock(Node.class);
        when(page.getJCRNode()).thenReturn(jcrPage);
        final Session session = mock(Session.class);
        when(jcrPage.getSession()).thenReturn(session);
        when(jcrPage.getPath()).thenReturn("/myPage");
        final Workspace workspace = mock(Workspace.class);
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getName()).thenReturn("test");

        aggState.setMainContent(page.getJCRNode());
        when(magnoliaCtx.getAggregationState()).thenReturn(aggState);
        final HierarchyManager hm = mock(HierarchyManager.class);
        when(magnoliaCtx.getHierarchyManager("test")).thenReturn(hm);
        when(hm.getWorkspace()).thenReturn(workspace);
        when(hm.getContent("/myPage")).thenReturn(page);
        when(workspace.getSession()).thenReturn(session);

        final Map templateCtx = new HashMap();
        final JspParagraphRenderer renderer = new JspParagraphRenderer();

        // ugly hack to execute renderer.setupContext()
        Method setupContextMethod = AbstractRenderer.class.getDeclaredMethod("setupContext", new Class[]{Map.class, Content.class, RenderableDefinition.class, RenderingModel.class, Object.class});
        setupContextMethod.setAccessible(true);
        setupContextMethod.invoke(renderer, new Object[]{templateCtx, page, null, null, null});

        // other tests should verify the other objects !
        assertEquals("Unexpected amount of objects in context", 7, templateCtx.size());
        assertTrue(templateCtx.get("actpage") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("actpage")));

        assertTrue(templateCtx.get("content") instanceof Map);
        assertEquals(page, unwrap((Content) templateCtx.get("content")));

    }

    private Content unwrap(Content c) {
        if (c instanceof ContentWrapper) {
            return unwrap(((ContentWrapper) c).getWrappedContent());
        }
        return c;
    }
}
