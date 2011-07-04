/**
 * This file Copyright (c) 2011 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.rendering.context;

import java.util.EmptyStackException;
import javax.jcr.Node;

import org.junit.Test;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.rendering.template.RenderableDefinition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;


/**
 * @version $Id$
 */
public class AggregationStateBasedRenderingContextTest {

    @Test
    public void testGetMainContent() {
        // given
        AggregationState aggregationState = new AggregationState();
        Node mainContent = mock(Node.class);
        aggregationState.setMainContent(mainContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        Node result = context.getMainContent();

        // then
        assertEquals(mainContent, result);
    }

    @Test
    public void testGetCurrentContent() {
        // given
        AggregationState aggregationState = new AggregationState();
        Node currentContent = mock(Node.class);
        aggregationState.setCurrentContent(currentContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        Node result = context.getCurrentContent();

        // then
        assertEquals(currentContent, result);
    }

    @Test
    public void testPushSetsMainContentIfItsNull() {
        // given
        AggregationState aggregationState = new AggregationState();
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        context.push(content, null);

        // then - mainContent should now be set
        assertEquals(content, context.getMainContent());
    }

    @Test
    public void testPushDoesNotSetMainContentIfItsNotNull() {
        // given
        Node mainContent = mock(Node.class);
        AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(mainContent);
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        context.push(content, null);

        // then - mainContent should be unchanged
        assertEquals(mainContent, context.getMainContent());
    }


    @Test
    public void testPushSetsCurrentContent() {
        // given
        AggregationState aggregationState = new AggregationState();
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        context.push(content, null);

        // then
        assertEquals(content, context.getCurrentContent());
    }

    @Test
    public void testPushSetsRenderableDefinition() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node content = mock(Node.class);
        RenderableDefinition renderableDefinition = mock(RenderableDefinition.class);

        // when
        context.push(content, renderableDefinition);

        // then
        assertEquals(renderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPop() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node first = mock(Node.class);
        Node second = mock(Node.class);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition);
        context.push(second, secondRenderableDefinition);

        // when
        context.pop();

        // then
        assertEquals(first, context.getCurrentContent());
        assertEquals(firstRenderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPopWithThreeLevels() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node first = mock(Node.class);
        Node second = mock(Node.class);
        Node third = mock(Node.class);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition thirdRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition);
        context.push(second, secondRenderableDefinition);
        context.push(third, thirdRenderableDefinition);

        // then
        assertEquals(third, context.getCurrentContent());
        assertEquals(thirdRenderableDefinition, context.getRenderableDefinition());

        // when
        context.pop();

        // then
        assertEquals(second, context.getCurrentContent());
        assertEquals(secondRenderableDefinition, context.getRenderableDefinition());

        // when
        context.pop();

        // then
        assertEquals(first, context.getCurrentContent());
        assertEquals(firstRenderableDefinition, context.getRenderableDefinition());
    }

    @Test(expected = EmptyStackException.class)
    public void testPopWithoutPrecedingPush() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // when
        context.pop();

        // then - nothing on stack so there should be a Exception
    }

    @Test
    public void testGetRenderableDefinition() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node first = mock(Node.class);
        Node second = mock(Node.class);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition);
        context.push(second, secondRenderableDefinition);

        // when
        RenderableDefinition result = context.getRenderableDefinition();

        // then
        assertEquals(result, secondRenderableDefinition);
    }

    @Test
    public void testGetRenderableDefinitionBeforePushReturnsNull() {
        // given
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        // when
        RenderableDefinition result = context.getRenderableDefinition();

        // then
        assertNull(result);
    }
}
