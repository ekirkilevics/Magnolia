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

import com.google.inject.util.Providers;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.rendering.template.RenderableDefinition;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;


/**
 * @version $Id$
 */
public class AggregationStateBasedRenderingContextTest {

    @Test
    public void usesAggregationStateFromProvider() {
        // GIVEN
        Node mainContent = mock(Node.class);
        AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(mainContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(Providers.of(aggregationState));

        // WHEN
        Node returnedMainContent = context.getMainContent();

        // THEN
        assertSame(mainContent, returnedMainContent);
    }

    @Test
    public void testGetMainContent() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node mainContent = mock(Node.class);
        aggregationState.setMainContent(mainContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        Node result = context.getMainContent();

        // THEN
        assertEquals(mainContent, result);
    }

    @Test
    public void testGetCurrentContent() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node currentContent = mock(Node.class);
        aggregationState.setCurrentContent(currentContent);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        Node result = context.getCurrentContent();

        // THEN
        assertEquals(currentContent, result);
    }

    @Test
    public void testPushSetsMainContentIfItsNull() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        context.push(content, null);

        // THEN - mainContent should now be set
        assertEquals(content, context.getMainContent());
    }

    @Test
    public void testPushDoesNotSetMainContentIfItsNotNull() {
        // GIVEN
        Node mainContent = mock(Node.class);
        AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(mainContent);
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        context.push(content, null);

        // THEN - mainContent should be unchanged
        assertEquals(mainContent, context.getMainContent());
    }


    @Test
    public void testPushSetsCurrentContent() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        Node content = mock(Node.class);
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);

        // WHEN
        context.push(content, null);

        // THEN
        assertEquals(content, context.getCurrentContent());
    }

    @Test
    public void testPushSetsRenderableDefinition() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node content = mock(Node.class);
        RenderableDefinition renderableDefinition = mock(RenderableDefinition.class);

        // WHEN
        context.push(content, renderableDefinition);

        // THEN
        assertEquals(renderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPop() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node first = mock(Node.class);
        Node second = mock(Node.class);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition);
        context.push(second, secondRenderableDefinition);

        // WHEN
        context.pop();

        // THEN
        assertEquals(first, context.getCurrentContent());
        assertEquals(firstRenderableDefinition, context.getRenderableDefinition());
    }

    @Test
    public void testPopWithThreeLevels() {
        // GIVEN
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
    public void testGetRenderableDefinition() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        AggregationStateBasedRenderingContext context = new AggregationStateBasedRenderingContext(aggregationState);
        Node first = mock(Node.class);
        Node second = mock(Node.class);
        RenderableDefinition firstRenderableDefinition = mock(RenderableDefinition.class);
        RenderableDefinition secondRenderableDefinition = mock(RenderableDefinition.class);
        context.push(first, firstRenderableDefinition);
        context.push(second, secondRenderableDefinition);

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
