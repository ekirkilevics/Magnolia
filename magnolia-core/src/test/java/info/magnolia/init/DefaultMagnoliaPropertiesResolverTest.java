/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.init;

import info.magnolia.context.MgnlContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.TestMagnoliaInitPaths;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DefaultMagnoliaPropertiesResolverTest {

    private ServletContext ctx;
    private MagnoliaInitPaths initPaths;

    @Before
    public void setUp() throws Exception {
        ctx = createStrictMock(ServletContext.class);
        initPaths = new TestMagnoliaInitPaths("test-host-name", "/tmp/magnoliaTests", "magnoliaTests", "/context/path");
    }

    @After
    public void tearDown() throws Exception {
        verify(ctx);
        System.getProperties().remove("testProp");
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
    }

    @Test
    public void testDefaultLocations() {
        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn(null);
        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        final List<String> expected = Arrays.asList(
                "WEB-INF/config/test-host-name/context/path/magnolia.properties",
                "WEB-INF/config/test-host-name/magnoliaTests/magnolia.properties",
                "WEB-INF/config/test-host-name/magnolia.properties",
                "WEB-INF/config/context/path/magnolia.properties",
                "WEB-INF/config/magnoliaTests/magnolia.properties",
                "WEB-INF/config/default/magnolia.properties",
                "WEB-INF/config/magnolia.properties");
        assertEquals(7, locations.size());
        assertEquals(expected, locations);
    }

    @Test
    public void testLocationsAreTrimmed() {
        final String valueInWebXml = "  location1 , location2\n\t,\tlocation3,,,location4\n,\nlocation5\t,\n\tlocation6   ,\n location7\n\n";
        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn(valueInWebXml);
        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        final List<String> expected = Arrays.asList("location1", "location2", "location3", "location4", "location5", "location6", "location7");
        assertEquals(7, locations.size());
        assertEquals(expected, locations);
    }

    /**
     * Two tests in one:
     * testInitParamCanMixAbsoluteAndRelativePaths()
     * testSourcesOnlyIncludeExistingLocations()
     */
    @Test
    public void testSourcesOnlyIncludeExistingLocations() {
        final String unexistingAbsPath = "/lol/pouet/magnolia.properties";
        final String unexistingRelPath = "rel/dummy/path/magnolia.properties";
        final String existingRelPath = "WEB-INF/hello/magnolia.properties";
        final String existingAbsPath = getClass().getResource("/test-magnolia.properties").getFile();

        assertTrue("This test can't run, a path we hoped would exist does not: " + existingAbsPath, new File(existingAbsPath).exists());
        assertFalse("This test can't run, a path we hoped would not exist does: " + unexistingAbsPath, new File(unexistingAbsPath).exists());

        //some existing FS path, some existing ctx path, some unexisting rel and abs paths
        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/hello/magnolia.properties," + unexistingAbsPath + "," + existingAbsPath + "," + unexistingRelPath);
        // the paths have to be absolute, but they are relative to the webapp folder
        expect(ctx.getResourceAsStream("/" + existingRelPath)).andReturn(getClass().getResourceAsStream("/test-init.properties"));
        expect(ctx.getResourceAsStream("/" + unexistingRelPath)).andReturn(null);
        replay(ctx);
        final List<PropertySource> sources = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getSources();
        assertEquals(2, sources.size());
        assertEquals("[ServletContextPropertySource from WEB-INF/hello/magnolia.properties]", sources.get(0).describe());
        assertEquals("[FileSystemPropertySource from " + existingAbsPath + "]", sources.get(1).describe());
    }

    @Test
    public void testFileResolutionCtxAttributes() {

        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${contextParam/param}/${contextAttribute/attribute}/magnolia.properties");
        expect(ctx.getAttribute("attribute")).andReturn("attributevalue");
        expect(ctx.getInitParameter("param")).andReturn("paramvalue");

        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        assertEquals(1, locations.size());
        assertEquals("WEB-INF/paramvalue/attributevalue/magnolia.properties", locations.get(0));
    }

    @Test
    public void testSystemPropertiesCanBeUsed() {
        System.setProperty("testProp", "hello");
        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${systemProperty/testProp}/magnolia.properties");
        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        assertEquals(1, locations.size());
        assertEquals("WEB-INF/hello/magnolia.properties", locations.get(0));
    }

    @Test
    public void testEnvironmentPropertiesCanBeUsed() {
        // since we can't add properties to System.env, we just an env property whose value we can know otherwise.
        // This test will most likely fail on Windows (where the env property seems to be USERNAME), unless someone comes up with a brighter idea.
        final String user = System.getProperty("user.name");

        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${env/USER}/magnolia.properties");
        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        assertEquals(1, locations.size());
        assertEquals("WEB-INF/" + user + "/magnolia.properties", locations.get(0));
    }


    @Test
    public void unexistingContextParamsAttributesAndPropertiesAreNotSubstituted() {
        assertNull("Can't run this test if a system property called 'myProp' does exist.", System.getProperty("mySysProp"));
        assertNull("Can't run this test if an environment property called 'myEnvProp' does exist.", System.getenv("myEnvProp"));

        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${contextParam/myParam}/${contextAttribute/myAttr}/${systemProperty/mySysProp}/${env/myEnvProp}/magnolia.properties,WEB-INF/config/default/magnolia.properties");
        expect(ctx.getAttribute("myAttr")).andReturn(null);
        expect(ctx.getInitParameter("myParam")).andReturn(null);

        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        assertEquals(2, locations.size());
        assertEquals("WEB-INF/${contextParam/myParam}/${contextAttribute/myAttr}/${systemProperty/mySysProp}/${env/myEnvProp}/magnolia.properties", locations.get(0));
        assertEquals("WEB-INF/config/default/magnolia.properties", locations.get(1));
    }

    @Test
    public void testFileResolutionWithContextPath() {

        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${contextPath}/magnolia.properties");

        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, initPaths).getLocations();
        assertEquals(1, locations.size());
        assertEquals("WEB-INF/context/path/magnolia.properties", locations.get(0));
    }

    @Test
    public void testFileResolutionWithRootContextPath() {

        expect(ctx.getInitParameter("magnolia.initialization.file")).andReturn("WEB-INF/${contextPath}/magnolia.properties");

        TestMagnoliaInitPaths magnoliaInitPaths = new TestMagnoliaInitPaths("test-host-name", "/tmp/magnoliaTests", "magnoliaTests", "");

        replay(ctx);
        final List<String> locations = new DefaultMagnoliaPropertiesResolver(ctx, magnoliaInitPaths).getLocations();
        assertEquals(1, locations.size());
        assertEquals("WEB-INF/ROOT/magnolia.properties", locations.get(0));
    }
}
