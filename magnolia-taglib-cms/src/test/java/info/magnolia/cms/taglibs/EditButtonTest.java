/**
 * This file Copyright (c) 2009-2011 Magnolia International
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
package info.magnolia.cms.taglibs;

import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.Permission;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.test.MgnlTagTestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;

import com.mockrunner.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.util.Collections;

/**
 * @author had
 * @version $Revision: $ ($Author: $)
 */
public class EditButtonTest extends MgnlTagTestCase {
    private Content mainContent;
    private Content currentContent;

    protected HierarchyManager initWebsiteData() throws IOException, RepositoryException {
        return null;
    }

    protected void setUp() throws Exception {
        super.setUp();

        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAdmin(true);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);

        mainContent = createMock(Content.class);
        currentContent = createMock(Content.class);

        final AggregationState aggregationState = new AggregationState();
        aggregationState.setMainContent(this.mainContent);
        aggregationState.setCurrentContent(this.currentContent);

        webContext.setAggregationState(aggregationState);
        webContext.setParameters(Collections.singletonMap(Resource.MGNL_PREVIEW_ATTRIBUTE, "false"));
        webContext.setRequest(new MockHttpServletRequest());
    }

    public void testDisplaysDialogWithCurrentNodaHandle() throws Exception {
        final String dialogName = "test-dial";
        final String name = "bar";
        final String handle = "/foo/" + name;

        expect(mainContent.isGranted(Permission.SET)).andReturn(true).anyTimes();
        expect(currentContent.getName()).andReturn("bar");
        expect(currentContent.getHandle()).andReturn(handle);
        replay(mainContent, currentContent);

        final EditButton tag = new EditButton();
        tag.setDialog(dialogName);

        tag.setPageContext(pageContext);
        tag.doEndTag();
        final String output = getJspOutput();
        assertTrue("Output should contain the dialog name and _current_ node handle", output.contains("mgnlOpenDialog('"+handle+"','','"+name+"','"+dialogName+"'"));
        // sanity check
        assertNotSame(MgnlContext.getAggregationState().getMainContent(), MgnlContext.getAggregationState().getCurrentContent());

        verify(mainContent, currentContent);
    }

}
