/**
 * This file Copyright (c) 2012 Magnolia International
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
package info.magnolia.module.admininterface;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.security.AccessManager;
import info.magnolia.cms.security.User;
import info.magnolia.cms.util.AlertUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.admininterface.pages.DevelopmentUtilsPage;
import info.magnolia.module.admininterface.pages.ExportPage;
import info.magnolia.module.admininterface.pages.ImportPage;
import info.magnolia.module.admininterface.pages.JCRUtilsPage;
import info.magnolia.repository.RepositoryManager;
import info.magnolia.test.ComponentsTestUtil;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.query.QueryManager;
import javax.security.auth.Subject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import junit.framework.TestCase;

import org.apache.commons.lang.StringEscapeUtils;
import org.junit.Test;

/**
 * Tests for XSS vulnerability in admincentral.
 */
public class AdmininterfaceXssTest extends TestCase {

    private RepositoryManager rm;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private WebContext context;
    private HierarchyManager hm;
    private Workspace workspace;
    private Session session;
    private AccessManager access;
    private QueryManager qm;
    private final int uiid = 0;
    private Content content;
    final String mgnlPath = "{{\"/><img src=x onerror=alert(/xss/)> }}";
    final String escapedPath = StringEscapeUtils.escapeHtml(mgnlPath);

    @Override
    protected void setUp() throws Exception {

        context = new ExtendsWebContext();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        hm = mock(HierarchyManager.class);
        workspace = mock(Workspace.class);
        session = mock(Session.class);
        access = mock(AccessManager.class);
        qm = mock(QueryManager.class);
        rm = mock(RepositoryManager.class);
        content = mock(Content.class);

        when(access.isGranted("/", 0)).thenReturn(false);
        when(session.getWorkspace()).thenReturn(workspace);
        when(workspace.getQueryManager()).thenReturn(qm);
        when(workspace.getSession()).thenReturn(session);
        when(hm.getContent(null)).thenReturn(content);
        when(hm.getWorkspace()).thenReturn(workspace);

        ComponentsTestUtil.setImplementation(AlertUtil.class, AlertUtil.class);
        ComponentsTestUtil.setInstance(RepositoryManager.class, rm);
        MgnlContext.setInstance(context);
    }

    @Override
    public void tearDown(){
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }

    @Test
    public void testDevelopmentUtilsPageXSS() throws IOException {

        //GIVEN
        DevelopmentUtilsPage dup = new DevelopmentUtilsPage(null, null, null);
        dup.setRootdir(mgnlPath);

        //WHEN
        dup.backupChildren();
        //THEN
        assertEquals(
                "Backup done to " + new File(Path.getAbsoluteFileSystemPath(escapedPath)).getCanonicalPath(),
                AlertUtil.getMessage()
        );

        //WHEN
        dup.backup();
        //THEN
        assertEquals(
                "Backup done to " + new File(Path.getAbsoluteFileSystemPath(escapedPath)).getCanonicalPath(),
                AlertUtil.getMessage()
        );
    }

    @Test
    public void testJCRUtilsPageXSS() {
        //GIVEN
        JCRUtilsPage up = new JCRUtilsPage(null, null, null);
        up.setRepository("repository");
        up.setPath(mgnlPath);
        //WHEN
        final String retValue = up.dump();
        //THEN
        assertEquals("path not found: " + escapedPath, retValue);
    }

    @Test
    public void testXmlImportXSS() throws Exception {
        //GIVEN
        ImportPage importXml = new ImportPage("import", request, response);
        importXml.setMgnlPath(mgnlPath);
        //WHEN
        try {
            importXml.importxml();
        } catch (ServletException e) {
            //this is expected to happen
        }
        //THEN
        assertEquals(
                "Write permission needed for import. User not allowed to WRITE path [" + escapedPath + "]",
                AlertUtil.getMessage()
        );
    }

    @Test
    public void testXmlExportXSS() throws Exception {
        //GIVEN
        ExportPage exportXml = new ExportPage("export", request, response);
        exportXml.setMgnlPath(mgnlPath);
        //WHEN
        try {
            exportXml.exportxml();
        } catch (ServletException e) {
            //this is expected to happen
        }
        //THEN
        assertEquals(
                "Write permission needed for export. User not allowed to WRITE path [" + escapedPath + "]",
                AlertUtil.getMessage()
        );
    }

    final class ExtendsWebContext implements WebContext {

        private String msg;

        @Override
        public Session getJCRSession(String workspaceName) throws LoginException, RepositoryException {
            return session;
        }

        @Override
        public <T> T getAttribute(String name, int scope) {
            return (T)msg;
        }

        @Override
        public void setAttribute(String name, Object value, int scope) {
            msg = (String) value;
        }

        @Override
        public AccessManager getAccessManager(String repository) {
            return access;
        }

        @Override
        public User getUser() {
            return null;
        }

        @Override
        public Subject getSubject() {
            return null;
        }

        @Override
        public void setLocale(Locale locale) {}

        @Override
        public Locale getLocale() {
            return null;
        }

        @Override
        @Deprecated
        public HierarchyManager getHierarchyManager(String workspaceName) {
            return hm;
        }

        @Override
        @Deprecated
        public info.magnolia.cms.core.search.QueryManager getQueryManager(String workspaceName) {
            return null;
        }

        @Override
        public <T> T getAttribute(String name) {
            return null;
        }

        @Override
        public Map<String, Object> getAttributes(int scope) {
            return null;
        }

        @Override
        public void removeAttribute(String name, int scope) {}

        @Override
        public Map<String, Object> getAttributes() {
            return null;
        }

        @Override
        public Messages getMessages() {
            return null;
        }

        @Override
        public Messages getMessages(String basename) {
            return null;
        }

        @Override
        public void release() {}

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            return null;
        }

        @Override
        public Object put(Object key, Object value) {
            return null;
        }

        @Override
        public Object remove(Object key) {
            return null;
        }

        @Override
        public void putAll(Map m) {}

        @Override
        public void clear() {}

        @Override
        public Set keySet() {
            return null;
        }

        @Override
        public Collection values() {
            return null;
        }

        @Override
        public Set entrySet() {
            return null;
        }

        @Override
        public void init(HttpServletRequest request,HttpServletResponse response, ServletContext servletContext) {}

        @Override
        public AggregationState getAggregationState() {
            return null;
        }

        @Override
        public void resetAggregationState() {}

        @Override
        public MultipartForm getPostedForm() {
            return null;
        }

        @Override
        public String getParameter(String name) {
            return null;
        }

        @Override
        public Map<String, String> getParameters() {
            return null;
        }

        @Override
        public String getContextPath() {
            return null;
        }

        @Override
        public HttpServletRequest getRequest() {
            return null;
        }

        @Override
        public HttpServletResponse getResponse() {
            return null;
        }

        @Override
        public ServletContext getServletContext() {
            return null;
        }

        @Override
        public void include(String path, Writer out) throws ServletException,IOException {}

        @Override
        public void setPageContext(PageContext pageContext) {}

        @Override
        public PageContext getPageContext() {
            return null;
        }

        @Override
        public void push(HttpServletRequest request,HttpServletResponse response) {}

        @Override
        public void pop() {}

        @Override
        public String[] getParameterValues(String name) {
            return null;
        }
    }
}
