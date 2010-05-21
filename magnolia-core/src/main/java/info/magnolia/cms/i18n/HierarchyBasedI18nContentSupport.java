/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.cms.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;

import java.util.Locale;

import org.apache.commons.lang.LocaleUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serves i18n content found in a per locale hierarchy structure.
 * E.g.
 * <pre>
 * +my-website
 *  + <strong>en</strong>
 *   + page-1
 *   + page-2
 *   + page-n
 *  + <strong>de</strong>
 *   + page-1
 *   + page-2
 *   + page-n
 *  + <strong>de_CH</strong>
 *   + page-1
 *   + page-2
 *   + page-n
 *  + <strong>it</strong>
 *   + page-1
 *   + page-2
 *   + page-n
 *</pre>
 * The locale is inferred by analyzing the URI and checking whether it contains a valid Java locale code (for a definition of valid <a href="http://java.sun.com/j2se/1.5.0/docs/api/java/util/Locale.html">Java locale</a> code).
 * This code can be at whatever position in the URI, not necessarily the first one. For example, given the following URI <em>/my-website/node-1/node-2/de/home-page.html</em>, the locale <em>de</em>
 * will be detected and content served from here:
 * <pre>
 * + my-website
 *  + node-1
 *   + node-2
 *    + <strong>de</strong>
 *     + home-page
 * </pre>
 * If no locale is found in the URI, the default one is assumed.
 * @author fgrilli
 *
 */
public class HierarchyBasedI18nContentSupport extends AbstractI18nContentSupport {

    private static final Logger log = LoggerFactory.getLogger(HierarchyBasedI18nContentSupport.class);
    @Override
    protected Locale onDetermineLocale() {
        Locale locale = null;
        Locale validUnsupportedLocale = null;
        final String i18nURI = MgnlContext.getAggregationState().getCurrentURI();
        log.debug("URI to check for locales is {}", i18nURI);
        final String[] splitURI = i18nURI.split("/");
        for(String uriToken: splitURI){
            locale = determineLocalFromString(uriToken);
            if(LocaleUtils.isAvailableLocale(locale)){
                log.debug("found a valid Locale code {}", uriToken);
                if(isLocaleSupported(locale)){
                    break;
                } else {
                    //the URI contains a valid Locale code but it is not supported by the current I18n configuration.
                    //We store it anyway and eventually return it if no exact match will be found at the end of this loop.
                    validUnsupportedLocale = locale;
                }
            }
            locale = null;
        }
        return locale != null ? locale : validUnsupportedLocale;
    }

    /*
     * Just return the uri, no need to add the locale to it
     *  (non-Javadoc)
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport#toI18NURI(java.lang.String, java.util.Locale)
     */
    @Override
    protected String toI18NURI(String uri, Locale locale) {
        return uri;
    }

    /*
     * Just return the uri, no need to remove the locale from it
     *  (non-Javadoc)
     * @see info.magnolia.cms.i18n.AbstractI18nContentSupport#toRawURI(java.lang.String, java.util.Locale)
     */
    @Override
    protected String toRawURI(String i18nURI, Locale locale) {
        return i18nURI;
    }

    @Override
    public NodeData getNodeData(Content node, String name) {
        // return the node data
        return node.getNodeData(name);
    }
}
