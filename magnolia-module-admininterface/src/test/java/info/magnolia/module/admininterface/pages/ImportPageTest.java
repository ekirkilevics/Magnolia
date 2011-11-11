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

        assertEquals(false, importFile.getFile().exists());
    }

}
