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
package info.magnolia.cms.beans.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Virtual uri mapping implementation that uses regular expressions in fromURI/toURI. When using regular expression in
 * <code>fromURI</code>, <code>toURI</code> can contain references to the regexp matches. For example:
 *
 * <pre>
 * fromURI=/products/([0-9A-Z]+)\.html
 * toURI=/product/detail.html?productId=$1
 * </pre>
 *
 * @author Fabrizio Giustina
 * @author philipp
 * @version $Id: DefaultVirtualURIMapping.java 10295 2007-08-02 21:33:58Z fgiust $
 */
public class RegexpVirtualURIMapping implements VirtualURIMapping {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegexpVirtualURIMapping.class);

    private String fromURI;
    private String toURI;
    private Pattern regexp;

    public MappingResult mapURI(final String uri) {

        if (regexp != null) {
            final Matcher matcher = regexp.matcher(uri);
            if (matcher.find()) {
                final MappingResult r = new MappingResult();
                final int matcherCount = matcher.groupCount();
                try {
                    final String replaced = matcher.replaceAll(toURI);

                    r.setLevel(matcherCount + 1);
                    r.setToURI(replaced);
                    return r;
                } catch (IndexOutOfBoundsException e) {
                    log.warn("{} misconfigured: {}", toString(), e.getMessage());
                }
            }
        }

        return null;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;

        this.regexp = Pattern.compile(fromURI);
    }

    public String getToURI() {
        return this.toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    public String toString() {
        return "RegexpVirtualURIMapping[" + fromURI + " --> " + toURI + "]";
    }

}
