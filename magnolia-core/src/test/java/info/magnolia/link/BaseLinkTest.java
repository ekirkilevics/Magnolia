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
package info.magnolia.link;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.URI2RepositoryManager;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.i18n.DefaultI18nContentSupport;
import info.magnolia.cms.i18n.I18nContentSupport;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.BinaryMockNodeData;
import info.magnolia.test.mock.MockContent;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.context.WebContext;
import info.magnolia.context.MgnlContext;
import static org.easymock.classextension.EasyMock.*;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author philipp
 * @version $Id$
 *
 */
public abstract class BaseLinkTest extends MgnlTestCase {

    protected static final String SOME_CONTEXT = "/some-context";
    protected static final String HANDLE_PARENT_SUB = "/parent/sub";
    protected static final String UUID_PATTERN_OLD_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',workspace:'{'default'}',path:'{'{2}'}}}'";
    protected static final String UUID_PATTERN_NEW_FORMAT = "$'{'link:'{'uuid:'{'{0}'}',repository:'{'{1}'}',handle:'{'{2}'}',nodeData:'{'{3}'}',extension:'{'{4}'}}}'";
    protected static final String UUID_PATTERN_SIMPLE = MessageFormat.format(UUID_PATTERN_NEW_FORMAT, new String[]{"2", ContentRepository.WEBSITE, HANDLE_PARENT_SUB, "", "html"});
    protected static final String UUID_PATTERN_SIMPLE_OLD_FORMAT = MessageFormat.format(UUID_PATTERN_OLD_FORMAT, new String[]{"2", ContentRepository.WEBSITE, HANDLE_PARENT_SUB});

    protected static final String HREF_ABSOLUTE_LINK = HANDLE_PARENT_SUB + ".html";

    protected WebContext webContext;

    protected List allMocks;

    protected String website =
        "/parent@uuid=1\n" +
        "/parent/sub@uuid=2\n" +
        "/parent/sub2@uuid=3";


    protected void setUp() throws Exception {
        super.setUp();

        MockHierarchyManager hm = MockUtil.createHierarchyManager(website);
        hm.setName(ContentRepository.WEBSITE);
        webContext = createMock(WebContext.class);
        expect(webContext.getHierarchyManager(ContentRepository.WEBSITE)).andReturn(hm).anyTimes();
        expect(webContext.getContextPath()).andReturn(SOME_CONTEXT).anyTimes();

        // add a binary
        MockContent page = (MockContent) hm.getContent(HANDLE_PARENT_SUB);
        page.addNodeData(new BinaryMockNodeData("file", null, "test.jpg", "image/jpeg", 5000));

        allMocks = new ArrayList();
        allMocks.add(webContext);
        MgnlContext.setInstance(webContext);

        // not configured in the repository
        ComponentsTestUtil.setImplementation(URI2RepositoryManager.class, URI2RepositoryManager.class);

        ComponentsTestUtil.setInstance(I18nContentSupport.class, new DefaultI18nContentSupport());

        ComponentsTestUtil.setInstance(LinkTransformerManager.class, new LinkTransformerManager());

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setDefaultBaseUrl("http://myTests:1234/yay");
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
