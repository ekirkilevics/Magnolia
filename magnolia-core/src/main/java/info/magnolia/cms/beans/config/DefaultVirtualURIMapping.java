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
package info.magnolia.cms.beans.config;

import info.magnolia.cms.util.SimpleUrlPattern;
import info.magnolia.cms.util.UrlPattern;


/**
 * @author philipp
 * @version $Id$
 */
public class DefaultVirtualURIMapping implements VirtualURIMapping {

    private String fromURI;

    private UrlPattern pattern;

    private String toURI;

    public MappingResult mapURI(String uri) {

        if (pattern != null && pattern.match(uri)) {
            MappingResult r = new MappingResult();
            r.setLevel(pattern.getLength());
            r.setToURI(toURI);
            return r;
        }
        return null;
    }

    public String getFromURI() {
        return this.fromURI;
    }

    public void setFromURI(String fromURI) {
        this.fromURI = fromURI;
        this.pattern = new SimpleUrlPattern(fromURI);
    }

    public String getToURI() {
        return this.toURI;
    }

    public void setToURI(String toURI) {
        this.toURI = toURI;
    }

    public String toString() {
        return fromURI + " --> " + toURI;
    }

}
