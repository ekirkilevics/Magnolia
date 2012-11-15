/**
 * This file Copyright (c) 2009-2012 Magnolia International
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

import static org.junit.Assert.*;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;

import org.junit.After;
import org.junit.Test;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class RegexpVirtualURIMappingTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testExample() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/products/([0-9a-z]+)\\.html");
        mapping.setToURI("/product/detail.html?productId=$1");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/products/magnolia.html");
        assertEquals("/product/detail.html?productId=magnolia", res.getToURI());
        assertEquals(2, res.getLevel());
    }

    @Test
    public void testExample2() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("^/stk-resources/comics/resources/comics/css/(.*)\\.css$");
        mapping.setToURI("forward:/docroot/comics/css/$1.css");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/stk-resources/comics/resources/comics/css/style.css");
        assertEquals("forward:/docroot/comics/css/style.css", res.getToURI());
    }

    @Test
    public void testExample3() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("^/stk-resources/comics/resources/comics/css(.*)/(.*)\\.css$");
        mapping.setToURI("forward:/docroot/comics/css/$2.css");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/stk-resources/comics/resources/comics/css\b/style.css");
        assertEquals("forward:/docroot/comics/css/style.css", res.getToURI());
    }

    @Test
    public void testRegexSubstitutionWorksAndLevelIsSetToGroupCountPlusOne() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/foo/(.*?)-([1-9]).html");
        mapping.setToURI("/bar.action?foo=$1&id=$2");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/foo/chalala-6.html");
        assertEquals("/bar.action?foo=chalala&id=6", res.getToURI());
        assertEquals(3, res.getLevel());
    }

    // see MAGNOLIA-2659
    @Test
    public void testSupportsMoreThan9Groups() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/(a)(b)(c)(d)(e)(f)(g)(h)(i)(j)(k)(l)(m)(n)(o)(p)(q)(r)(s)(t)(u)(v)(w)(x)(y)(z).html");
        mapping.setToURI("/bar.action?param=$9-$266$10");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/abcdefghijklmnopqrstuvwxyz.html");
        // there is no group #266, so we expect group 9, "-", group 26, followed by "6", followed by group 10
        assertEquals("/bar.action?param=i-z6j", res.getToURI());
        assertEquals(27, res.getLevel());
    }

    @Test
    public void testGracefullyFailingOnIncompleteConfig() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("");
        mapping.setToURI("/foo.action?param=$0&id=$5");

        final VirtualURIMapping.MappingResult res = mapping.mapURI("/foo/bar.html");
        assertEquals(null, res);
    }

    @Test
    public void testGracefullyFailingOnWrongRegexGroup() {
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/foo/([a-z]+)/detail/([0-9]+)\\.html");
        mapping.setToURI("/foo.action?param=$0&id=$5");

        final String inputUri = "/foo/bar/detail/123.html";
        assertTrue("test not behaving as expected", inputUri.matches(mapping.getFromURI()));
        final VirtualURIMapping.MappingResult res = mapping.mapURI(inputUri);
        assertEquals(null, res);
    }

    @Test
    public void testQueryStringIsBeingPassedThroughExample(){
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/news/(.*)");
        mapping.setToURI("http://noviny.cz/$1");
        
        final String inputUri = "/news/news.html";
        final String inputQueryString = "local=true&history=false&sport=true";
        final VirtualURIMapping.MappingResult res = mapping.mapURI(inputUri, inputQueryString);
        
        assertEquals("http://noviny.cz/news.html?local=true&history=false&sport=true", res.getToURI());
        assertEquals(2, res.getLevel());
    }
    
    @Test
    public void testQueryStringIsBeingPassedThroughExample2(){
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/products/([0-9a-z]+)\\.html\\?visible=(true|false)$");
        mapping.setToURI("forward:/shop/$1?dostupna=$2");
        
        final String inputUri = "/products/book.html";
        final String inputQueryString = "visible=false";
        final VirtualURIMapping.MappingResult res = mapping.mapURI(inputUri, inputQueryString);
        
        assertEquals("forward:/shop/book?dostupna=false", res.getToURI());
        assertEquals(3, res.getLevel());
    }
    
    @Test
    public void testQueryStringIsBeingPassedThroughExample3(){
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/products/([a-z]+)/pet/(.*)\\?((([0-9a-z]+)=([0-9]+)&)+)(paid)=(true|false)$");
        mapping.setToURI("http://petshop.com/$1/$7.html?$3yes=$8");
        
        final String inputUri = "/products/homeanimal/pet/checkout.htm";
        final String inputQueryString = "?dog=5&cat=10&skunk=50&paid=true";
        final VirtualURIMapping.MappingResult res = mapping.mapURI(inputUri, inputQueryString);
        
        assertEquals("http://petshop.com/homeanimal/paid.html?dog=5&cat=10&skunk=50&yes=true", res.getToURI());
        assertEquals(9, res.getLevel());
    }
    
    @Test
    public void testQueryStringMappingFailedOnWrongQueryString(){
        final RegexpVirtualURIMapping mapping = new RegexpVirtualURIMapping();
        mapping.setFromURI("/failed/string.html\\?([0-9a-z]+)=([0-9]+)");
        mapping.setToURI("http://bookshop.com/$1.html?piece=$2");
        
        final String inputUri = "/failed/string.html";
        final String inputQueryString = "godfather=true";
        final VirtualURIMapping.MappingResult res = mapping.mapURI(inputUri, inputQueryString);
        
        assertEquals(null, res);
    }
}
