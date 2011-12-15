/**
 * This file Copyright (c) 2011 Magnolia International
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
package info.magnolia.module.admininterface.pages;

import java.io.File;

import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;

import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;

/**
 * Basic test to verify deleting tmp file after importing.
 * @author milan
 *
 */
public class ImportPageTest extends TestCase{

    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebContext context;
    private HierarchyManager hm;
    private Workspace workspace;
    private Session session;
    private AccessManager access;

    private ImportPage importXml;

    private int uiid = 0;

    @Override
    protected void setUp() throws Exception {
        request = createStrictMock(HttpServletRequest.class);
        response = createStrictMock(HttpServletResponse.class);
        context = createStrictMock(WebContext.class);
        hm = createStrictMock(HierarchyManager.class);
        workspace = createStrictMock(Workspace.class);
        session = createNiceMock(Session.class);

        importXml = new ImportPage("import", request, response);
        MgnlContext.setInstance(context);
    }

    @Override
    public void tearDown(){
        MgnlContext.setInstance(null);
    }

    public void testXmlImport() throws Exception{

        File originalXml = new File(getClass().getResource("/test-import.xml").getFile());
        File tmpXml = new File(originalXml.getParent() + "/tmp-test-import.xml");
        FileUtils.copyFile(originalXml, tmpXml);
        Document importFile = new Document(tmpXml, "text/xml");

        expect(context.getAccessManager("website")).andReturn(access);
        expect(context.getHierarchyManager("website")).andReturn(hm);
        expect(hm.getWorkspace()).andReturn(workspace);
        expect(hm.isExist("/")).andReturn(true);
        expect(workspace.getSession()).andReturn(session);

        Object[] obj = new Object[]{request, response, context, hm, session, workspace};
        replay(obj);

        importXml.setMgnlFileImport(importFile);
        importXml.setMgnlKeepVersions(true);
        importXml.setMgnlUuidBehavior(uiid);

        importXml.importxml();

        verify(obj);

        assertFalse(importFile.getFile().exists());
    }
}
