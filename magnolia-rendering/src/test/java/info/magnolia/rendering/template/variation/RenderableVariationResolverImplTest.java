/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.rendering.template.variation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import info.magnolia.beanmerger.BeanMerger;
import info.magnolia.beanmerger.ProxyBasedBeanMerger;
import info.magnolia.cms.core.Channel;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.test.AbstractMagnoliaTestCase;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @version $Id$
 */
public class RenderableVariationResolverImplTest extends AbstractMagnoliaTestCase {

    private static final String CHANNEL = "smartphone";

    private static final String EXTENSION = "html";

    private static final String CHANNEL_EXTENSION = CHANNEL + "-" + EXTENSION;

    private ConfiguredTemplateDefinition templateDefinition;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setInstance(BeanMerger.class, new ProxyBasedBeanMerger());

        templateDefinition = new ConfiguredTemplateDefinition();
        templateDefinition.setTemplateScript("this-will-be-overridden-in-variation");
        templateDefinition.setDescription("this-will-be-reused-by-the-variation");

        ConfiguredTemplateDefinition variationChannelExtension = new ConfiguredTemplateDefinition();
        variationChannelExtension.setTemplateScript("this-is-an-overriding-templateScript-for-channel-extension");
        variationChannelExtension.setName(CHANNEL_EXTENSION);

        templateDefinition.addVariation(variationChannelExtension.getName(), variationChannelExtension);

        ConfiguredTemplateDefinition variationExtension = new ConfiguredTemplateDefinition();
        variationExtension.setTemplateScript("this-is-an-overriding-templateScript-for-extension");
        variationExtension.setName(EXTENSION);

        templateDefinition.addVariation(variationExtension.getName(), variationExtension);

        ConfiguredTemplateDefinition variationChannel = new ConfiguredTemplateDefinition();
        variationChannel.setTemplateScript("this-is-an-overriding-templateScript-for-channel");
        variationChannel.setName(CHANNEL);

        templateDefinition.addVariation(variationChannel.getName(), variationChannel);
    }

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        ComponentsTestUtil.clear();
    }

    @Test
    public void testChooseTemplateWhenVariationExistForChannelExtensionName() throws Exception {
        // GIVEN
        final MockWebContext mockContext = (MockWebContext) MockUtil.initMockContext();
        final Channel channel = new Channel();
        channel.setName(CHANNEL);
        mockContext.getAggregationState().setChannel(channel);
        mockContext.getAggregationState().setExtension(EXTENSION);
        final RenderableVariationResolverImpl resolver = new RenderableVariationResolverImpl();

        // WHEN
        RenderableDefinition result = resolver.resolveVariation(templateDefinition);

        // THEN
        assertEquals(CHANNEL_EXTENSION, result.getName());
        assertEquals("this-is-an-overriding-templateScript-for-channel-extension", result.getTemplateScript());
        assertEquals("this-will-be-reused-by-the-variation", result.getDescription());
    }

    @Test
    public void testChooseTemplateWhenVariationExistForExtensionName() throws Exception {
        // GIVEN
        final MockWebContext mockContext = (MockWebContext) MockUtil.initMockContext();
        mockContext.getAggregationState().setExtension(EXTENSION);
        final RenderableVariationResolverImpl resolver = new RenderableVariationResolverImpl();

        // WHEN
        RenderableDefinition result = resolver.resolveVariation(templateDefinition);

        // THEN
        assertEquals(EXTENSION, result.getName());
        assertEquals("this-is-an-overriding-templateScript-for-extension", result.getTemplateScript());
        assertEquals("this-will-be-reused-by-the-variation", result.getDescription());
    }

    @Test
    public void testChooseTemplateWhenVariationExistForChannelName() throws Exception {
        // GIVEN
        final MockWebContext mockContext = (MockWebContext) MockUtil.initMockContext();
        final Channel channel = new Channel();
        channel.setName(CHANNEL);
        mockContext.getAggregationState().setChannel(channel);
        final RenderableVariationResolverImpl resolver = new RenderableVariationResolverImpl();

        // WHEN
        RenderableDefinition result = resolver.resolveVariation(templateDefinition);

        // THEN
        assertEquals(CHANNEL, result.getName());
        assertEquals("this-is-an-overriding-templateScript-for-channel", result.getTemplateScript());
        assertEquals("this-will-be-reused-by-the-variation", result.getDescription());
    }
    
    @Test
    public void testDoesNothingWhenVariationDoesntExist() throws Exception {
        // GIVEN
        MockWebContext mockContext = (MockWebContext) MockUtil.initMockContext();
        final Channel channel = new Channel();
        channel.setName("channelDoesNotExist");
        mockContext.getAggregationState().setChannel(channel);
        mockContext.getAggregationState().setExtension("extensionDoesNotExist");
        final RenderableVariationResolverImpl resolver = new RenderableVariationResolverImpl();

        // WHEN
        RenderableDefinition result = resolver.resolveVariation(templateDefinition);

        // THEN
        assertNull(result);
    }

    @Test
    public void testDoesNothingWhenAggregationStateNotAvailable() throws Exception {
        // GIVEN
        final RenderableVariationResolverImpl resolver = new RenderableVariationResolverImpl();

        // WHEN
        final RenderableDefinition result = resolver.resolveVariation(templateDefinition);

        // THEN
        assertNull(result);
    }
}
