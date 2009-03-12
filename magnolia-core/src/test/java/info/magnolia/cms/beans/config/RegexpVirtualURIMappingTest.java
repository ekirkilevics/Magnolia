/**
 * This file Copyright (c) 2009 Magnolia International
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

import junit.framework.TestCase;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RegexpVirtualURIMappingTest extends TestCase {

    public void testExample() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/products/([0-9a-z]+)\\.html");
        mapping.setToURI("/product/detail.html?productId=$1");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/products/magnolia.html");
        assertEquals("/product/detail.html?productId=magnolia", res.getToURI());
        assertEquals(2, res.getLevel());
    }

    public void testRegexSubstitutionWorksAndLevelIsSetToGroupCountPlusOne() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/foo/(.*?)-([1-9]).html");
        mapping.setToURI("/bar.action?foo=$1&id=$2");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/foo/chalala-6.html");
        assertEquals("/bar.action?foo=chalala&id=6", res.getToURI());
        assertEquals(3, res.getLevel());
    }

    /* TODO - see MAGNOLIA-2659
    public void testSupportsMoreThan9Groups() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k)(l)(m)(n)(o)(p)(q)(r)(s)(t)(u)(v)(w)(x)(y)(z).html");
        mapping.setToURI("/bar.action?param=$9-$266$10");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/abcdefghijklmnopqrstuvwxyz.html");
        // there is no group #266, so we expect group 9, "-", group 26, followed by "6", followed by group 10
        assertEquals("/bar.action?param=i-z6j", res.getToURI());
        assertEquals(27, res.getLevel());
    }
    */
}
