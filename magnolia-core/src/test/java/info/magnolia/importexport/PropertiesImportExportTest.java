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
package info.magnolia.importexport;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockPageContext;
import com.mockrunner.mock.web.MockServletConfig;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.MetaData;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.cms.link.LinkResolver;
import info.magnolia.cms.link.LinkResolverImpl;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

import static org.easymock.EasyMock.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class PropertiesImportExportTest extends MgnlTestCase {
    private final PropertiesImportExport pie = new PropertiesImportExport();
    protected MockWebContext webContext;
    protected HierarchyManager hierarchyManager;
    protected MockPageContext pageContext;
    private SystemContext sysContext;


    protected void setUp() throws Exception {
        super.setUp();

        hierarchyManager = initWebsiteData();
        webContext = new MockWebContext();
        webContext.addHierarchyManager(ContentRepository.WEBSITE, hierarchyManager);

        MgnlContext.setInstance(webContext);

        sysContext = createMock(SystemContext.class);
        expect(sysContext.getLocale()).andReturn(Locale.ENGLISH).anyTimes();
        replay(sysContext);
        FactoryUtil.setInstance(SystemContext.class, sysContext);

        // set up necessary items not configured in the repository
        FactoryUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);
        FactoryUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());
        FactoryUtil.setInstance(LinkResolver.class, new LinkResolverImpl());

        setupPageContext();
    }

    protected void setupPageContext() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        pageContext = new MockPageContext(new MockServletConfig(), req, new MockHttpServletResponse());
    }

    public void testConvertsToStringByDefault() throws IOException, RepositoryException {
        assertEquals("foo", pie.convertNodeDataStringToObject("foo"));
        assertEquals("bar", pie.convertNodeDataStringToObject("string:bar"));
        assertEquals("2009-10-14T08:59:01.227-04:00", pie.convertNodeDataStringToObject("string:2009-10-14T08:59:01.227-04:00"));
    }

    public void testConvertsToWrapperType() {
        assertEquals(Boolean.TRUE, pie.convertNodeDataStringToObject("boolean:true"));
        assertEquals(Boolean.FALSE, pie.convertNodeDataStringToObject("boolean:false"));
        assertEquals(new Integer(5), pie.convertNodeDataStringToObject("integer:5"));
        final Object dateConvertedObject = pie.convertNodeDataStringToObject("date:2009-10-14T08:59:01.227-04:00");
        assertTrue(dateConvertedObject instanceof Calendar);
        assertEquals(1255525141227L, ((Calendar) dateConvertedObject).getTimeInMillis());
        // It's null if it doesn't match the exact format string
        final Object dateOnlyObject = pie.convertNodeDataStringToObject("date:2009-12-12");
        assertNull(dateOnlyObject);
    }

    public void testCanUseIntShortcutForConvertingIntegers() {
        assertEquals(new Integer(37), pie.convertNodeDataStringToObject("int:37"));
    }

    // This test uses the import then immediately exports and makes sure that the number of properties
    // at least matches. 

    public void testPropertiesExport() throws Exception {
        Properties baseProperties = new Properties();
        baseProperties.load(this.getClass().getResourceAsStream("propertiesimportexport-testrepo.properties"));

        Properties exportedProperties = PropertiesImportExport.contentToProperties(hierarchyManager.getRoot());
        //System.out.println(PropertiesImportExport.dumpPropertiesToString(exportedProperties));
        assertEquals("exported properties should be identical in size to imported properties",
                baseProperties.keySet().size(), exportedProperties.keySet().size()
        );

        Properties legacyExportedProperties = PropertiesImportExport.toProperties(hierarchyManager);
        //System.out.println(PropertiesImportExport.dumpPropertiesToString(legacyExportedProperties));
        assertEquals("Legacy mode export doesn't contain @uuid, metadata, or @type nodes",
                6, legacyExportedProperties.keySet().size()
        );
    }

    public void testImportMetadata() throws Exception {
        Content uuidLinkNode = hierarchyManager.getContentByUUID("2");
        assertNotNull("Content retrievable by its UUID", uuidLinkNode);
        MetaData nodeMetaData = uuidLinkNode.getMetaData();
        assertNotNull("Metadata of node should not be null when it is set explicitly in the properties", nodeMetaData);
        assertEquals("Template node is populated properly", "someParagraphName", nodeMetaData.getTemplate());
        assertTrue("activation matches status set in the properties", nodeMetaData.getIsActivated());

    }

    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return MockUtil.createHierarchyManager(this.getClass().getResourceAsStream("propertiesimportexport-testrepo.properties"));
    }

}
