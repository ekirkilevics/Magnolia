/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.context.MgnlContext;

import java.util.Locale;


import org.apache.commons.lang.StringUtils;

/**
 * This implementation support a language prefix like /en/*. To resolve the nodedatas it checks if a nodedata with the
 * following pattern exists on the content node: &lt;name&gt;_&lt;language&gt;
 * @author philipp
 * @version $Id$
 */
public class DefaultI18nContentSupport extends AbstractI18nContentSupport {

    /**
     * Adds the language prefix to the uri.
     */
    protected String toI18NURI(String uri, Locale locale) {
        // don't extend the uri for the default fallback language
        if(locale.equals(getDefaultLocale())){
            return uri;
        }
        
        // nothing to do for relative links
        if(uri.startsWith("/")){
            return "/" + locale.toString() + uri;
        }
        return uri;
    }

    protected String toRawURI(String i18nURI, Locale locale) {
        //MAGNOLIA-2142 - make sure we strip language only when it is actually present
        String raw = StringUtils.removeStart(i18nURI, "/" + locale.toString() + "/");
        // put back leading slash if removed while stripping language identifier
        return raw == null || raw.startsWith("/") ? raw : ("/" + raw);
    }

    protected Locale onDetermineLocale() {
        Locale locale;
        final String i18nURI = MgnlContext.getAggregationState().getCurrentURI();
        String localeStr = StringUtils.substringBetween(i18nURI, "/", "/");
        locale = determineLocalFromString(localeStr);
        return locale;
    }


}
