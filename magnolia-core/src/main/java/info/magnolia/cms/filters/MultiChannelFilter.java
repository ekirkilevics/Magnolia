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
package info.magnolia.cms.filters;

import info.magnolia.channel.ChannelConfiguration;
import info.magnolia.channel.ChannelManager;
import info.magnolia.channel.ChannelResolver;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Channel;
import info.magnolia.context.MgnlContext;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Inject;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter that resolves the site to use by considering variation of the set channel. Resulting site gets set in
 * {@link info.magnolia.cms.core.AggregationState}.
 *
 * @version $Id$
 */
public class MultiChannelFilter extends OncePerRequestAbstractMgnlFilter {
    private static final Logger log = LoggerFactory.getLogger(MultiChannelFilter.class);

    private ChannelManager channelManager;

    @Inject
    public MultiChannelFilter(ChannelManager channelManager) {
        this.channelManager = channelManager;
    }

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {

        AggregationState aggregationState = MgnlContext.getAggregationState();

        final Channel channel = new Channel();
        channel.setName(resolveChannel(request));

        aggregationState.setChannel(channel);

        chain.doFilter(request, response);
    }

    protected String resolveChannel(HttpServletRequest request) {
        ChannelConfiguration currentChannelConfig;
        ChannelResolver resolver;
        final Iterator<ChannelConfiguration> channelConfigIterator = channelManager.getChannels().values().iterator();
        String channelName = ChannelResolver.UNRESOLVED;

        // proceed as long as it could not be resolved - first match wins
        while (channelConfigIterator.hasNext() && channelName == ChannelResolver.UNRESOLVED) {
            currentChannelConfig = channelConfigIterator.next();
            resolver = currentChannelConfig.getResolver();

            if (resolver != null) {
                channelName = resolver.resolveChannel(request);
                log.debug("Type {} resolved channel to '{}'", resolver.getClass().getName(), channelName);
            }
        }

        return channelName == ChannelResolver.UNRESOLVED ? ChannelResolver.ALL : channelName;
    }
}
