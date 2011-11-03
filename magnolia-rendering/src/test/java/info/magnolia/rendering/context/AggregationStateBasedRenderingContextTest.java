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
package info.magnolia.rendering.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.rendering.template.RenderableDefinition;

import java.util.EmptyStackException;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.inject.util.Providers;


/**
 * @version $Id$
 */
public class AggregationStateBasedRenderingContextTest {

    private AggregationState aggregationState;
    private Node mainNode;
    private AggregationStateBasedRenderingContext context;
    private Session session;
    private Content mainContent;
    private Context ctx;
    private HierarchyManager hm;

    @After
    public void tearDown() {
        MgnlContext.setInstance(null);
    }

    @Before
    public void setUp() throws Exception {
        aggregationState = new AggregationState();
        mainNode = mock(Node.class);
        context = new AggregationStateBasedRenderingContext(aggregationState);
        session = mock(Session.class);
        when(mainNode.getSession()).thenReturn(session);
        Workspace wks = mock(Workspace.class);
        when(session.getWorkspace()).thenReturn(wks);
        when(wks.getName()).thenReturn("test");
        ctx = mock(Context.class);
        MgnlContext.setInstance(ctx);
        hm = mock(HierarchyManager.class);
        when(ctx.getHierarchyManager("test")).thenReturn(hm);
        when(hm.getWorkspace()).thenReturn(wks);
        when(wks.getSession()).thenReturn(session);
        when(mainNode.getPath()).thenReturn("/blah");
        mainContent = mock(Content.class);
        when(hm.getContent("/blah")).thenReturn(mainContent);
        when(mainContent.getJCRNode()).thenReturn(mainNode);

        // aggregation state expect current content to always exist!!!
        Content someContent = mock(Content.class);
        aggregationState.setCurrentContent(someContent);

    }

    @Test
    public void usesAggregationStateFromProvider() throws Exception {
        // GIVEN
        Node mainNode = mock(Node.class);
        Content mainContent = mock(Content.class);
        when(mainContent.getJCRNode()).thenReturn(mainNode);
        AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(mainContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(Providers.of(aggregationState));

        // WHEN
        Node returnedMainContent = context.getMainContent();

        // THEN
        assertSame(mainNode, returnedMainContent);
    }

    @Test
    public void testGetMainContent() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node mainNode = mock(Node.class);
        Content mainContent = mock(Content.class);
        when(mainContent.getJCRNode()).thenReturn(mainNode);
        aggregationState.setMainContent(mainContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        Node result = context.getMainContent();

        // THEN
        assertEquals(mainNode, result);
    }

    @Test
    public void testGetCurrentContent() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node currentNode = mock(Node.class);
        Content currentContent = mock(Content.class);
        when(currentContent.getJCRNode()).thenReturn(currentNode);
        aggregationState.setCurrentContent(currentContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        Node result = context.getCurrentContent();

        // THEN
        assertEquals(currentNode, result);
    }

    @Test
    public void testPushSetsMainContentIfItsNull() throws Exception {
        // GIVEN ... see setUp
        Content currentContent = mock(Content.class);
        aggregationState.setCurrentContent(currentContent);

        // WHEN
        context.push(mainNode, null, null);

        assertEquals(mainNode, context.getMainContent());
        // THEN - mainContent should now be set
    }

    @Test
    public void testPushDoesNotSetMainContentIfItsNotNull() throws Exception {
        // GIVEN ... see setUp()
        aggregationState.setMainContent(mainContent);
        aggregationState.setCurrentContent(mainContent);
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        when(content.getSession()).thenReturn(session);

        // WHEN
        context.push(content, null, null);

        // THEN - mainContent should be unchanged
        assertEquals(mainNode, context.getMainContent());
    }


    @Test
    public void testPushSetsCurrentContent() throws Exception {
        // GIVEN ... see setUp()
        Node node = mock(Node.class);
        when(node.getSession()).thenReturn(session);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        Content currentContent = mock(Content.class);
        when(node.getPath()).thenReturn("/boo");
        when(hm.getContent("/boo")).thenReturn(currentContent);
        when(currentContent.getJCRNode()).thenReturn(node);

        // WHEN
        context.push(node, null, null);

        // THEN
        assertEquals(node, context.getCurrentContent());
    }

    @Test
    public void testPushSetsRenderableDefinition() {
        // GIVEN
        RenderableDefinition renderableDefinition = mock(RenderableDefinition.class);

        // WHEN
        context.push(mainNode, renderableDefinition, null);

        // THEN
        assertEquals(renderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPop() throws Exception {
        // GIVEN
        Node second = mock(Node.class);
        when(second.getSession()).thenReturn(session);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(mainNode, firstRenderableDefinition, null);
        context.push(second, secondRenderableDefinition, null);

        // WHEN
        context.pop();

        // THEN
        assertEquals(mainNode, context.getCurrentContent());
        assertEquals(firstRenderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPopWithThreeLevels() throws Exception {
        // GIVEN ... see setUp()
        Node first = mock(Node.class);
        when(first.getSession()).thenReturn(session);
        when(first.getPath()).thenReturn("/first");
        Content firstContent = mock(Content.class);
        when(hm.getContent("/first")).thenReturn(firstContent);
        when(firstContent.getJCRNode()).thenReturn(first);
        Node second = mock(Node.class);
        when(second.getSession()).thenReturn(session);
        when(second.getPath()).thenReturn("/second");
        Content secondContent = mock(Content.class);
        when(hm.getContent("/second")).thenReturn(secondContent);
        when(secondContent.getJCRNode()).thenReturn(second);
        Node third = mock(Node.class);
        when(third.getSession()).thenReturn(session);
        when(third.getPath()).thenReturn("/third");
        Content thirdContent = mock(Content.class);
        when(hm.getContent("/third")).thenReturn(thirdContent);
        when(thirdContent.getJCRNode()).thenReturn(third);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition thirdRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition, null);
        context.push(second, secondRenderableDefinition, null);
        context.push(third, thirdRenderableDefinition, null);

        // THEN
        assertEquals(third, context.getCurrentContent());
        assertEquals(thirdRenderableDefinition, context.getRenderableDefinition());

        // WHEN
        context.pop();

        // THEN
        assertEquals(second, context.getCurrentContent());
        assertEquals(secondRenderableDefinition, context.getRenderableDefinition());

        // WHEN
        context.pop();

        // THEN
        assertEquals(first, context.getCurrentContent());
        assertEquals(firstRenderableDefinition, context.getRenderableDefinition());
    }

    @Test(expected = EmptyStackException.class)
    public void testPopWithoutPrecedingPush() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        context.pop();

        // THEN - nothing on stack so there should be a Exception
    }

    @Test
    public void testGetRenderableDefinition() throws Exception {
        // GIVEN
        Node second = mock(Node.class);
        when(second.getSession()).thenReturn(session);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(mainNode, firstRenderableDefinition, null);
        context.push(second, secondRenderableDefinition, null);

        // WHEN
        RenderableDefinition result = context.getRenderableDefinition();

        // THEN
        assertEquals(result, secondRenderableDefinition);
    }

    @Test
    public void testGetRenderableDefinitionBeforePushReturnsNull() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        // WHEN
        RenderableDefinition result = context.getRenderableDefinition();

        // THEN
        assertNull(result);
    }
}
