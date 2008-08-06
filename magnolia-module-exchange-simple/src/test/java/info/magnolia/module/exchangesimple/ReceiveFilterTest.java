/**
 * This file Copyright (c) 2003-2008 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 *
 * Any modifications to this file must keep this entire header
 * intact.
 *
 */
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ReceiveFilterTest extends TestCase {

    public void testActivateShouldCreateNewNodeIfItDoesNotExist() throws Exception {
        final HttpServletRequest request = createMock(HttpServletRequest.class); // not strict: we don't want to check method call order
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain filterChain = createStrictMock(FilterChain.class);
        final SystemContext sysCtx = createStrictMock(SystemContext.class);
        final WebContext ctx = createStrictMock(WebContext.class);
        final HierarchyManager hm = createMock(HierarchyManager.class);
        final Workspace workspace = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);
        final Content parentNode = createMock(Content.class); // TODO this should maybe be strict

        final InputStream xmlStream = getClass().getResourceAsStream("/resources4189.xml");
        final InputStream nodeStream = getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz");
        assertNotNull(xmlStream);
        assertNotNull(nodeStream);
        final MultipartForm form = new MultipartForm();
        // the file name (documents key) is only used here as the key of the map, is unrelated to actual name.
        form.getDocuments().put("blah.xml", new StreamOnlyDocument(xmlStream));
        form.getDocuments().put("node-to-activate.xml.gz", new StreamOnlyDocument(nodeStream));

        FactoryUtil.setInstance(SystemContext.class, sysCtx);
        MgnlContext.setInstance(ctx);

        final String parentPath = "/foo/bar";
        final String action = "activate";
        // checking headers
        expect(request.getHeader("mgnlExchangeAction")).andReturn(action).anyTimes();
        expect(request.getHeader("mgnlExchangeParentPath")).andReturn(parentPath).anyTimes();
        expect(request.getHeader("mgnlExchangeRepositoryName")).andReturn("some-repo").anyTimes();
        expect(request.getHeader("mgnlExchangeWorkspaceName")).andReturn("some-workspace").anyTimes();
        expect(request.getHeader("mgnlExchangeResourceMappingFile")).andReturn("blah.xml").anyTimes(); // this is hardcoded to resources.xml in BaseSyndicatorImpl
        expect(request.getHeader("Authorization")).andReturn(null).anyTimes();
        expect(request.getParameter("mgnlUserId")).andReturn("testuser").anyTimes();
        expect(sysCtx.getHierarchyManager("some-repo", "some-workspace")).andReturn(hm).anyTimes();

        // checking parent node
        expect(hm.getContent(parentPath)).andReturn(parentNode).anyTimes();
        expect(parentNode.isLocked()).andReturn(false).anyTimes();
        expect(parentNode.lock(true, true)).andReturn(null);

        expect(ctx.getAttribute("multipartform")).andReturn(form).anyTimes();

        // checking permissions
        // TODO : really, really, this should just crash
        //  - and we should actually check permissions and assert the behaviour of ReceiveFilter when the user does not have the appropriate permissions
        expect(hm.getAccessManager()).andReturn(null);

        // check node existence
        expect(hm.getContentByUUID("DUMMY-UUID")).andThrow(new ItemNotFoundException());

        // importing node
        expect(hm.getWorkspace()).andReturn(workspace);
        expect(workspace.getSession()).andReturn(session);
        session.importXML(eq(parentPath), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));
        expectLastCall().andAnswer(new IAnswer<Object>() {
            public Object answer() throws Throwable {
                final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                final InputStream expectedStream = new GZIPInputStream(new FileInputStream("/Users/gjoseph/Dev/magnolia/svn/magnolia/trunk/magnolia-module-exchange-simple/src/test/resources/exchange_threadReply4173.xml.gz"));
                assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                return null;
            }
        });
        hm.save();

        // response
        response.setHeader("sa_attribute_status", "sa_success");
        response.setHeader("sa_attribute_message", "");

        final ReceiveFilter filter = new ReceiveFilter();
        replay(request, response, filterChain, sysCtx, ctx, hm, workspace, session, parentNode);
        filter.doFilter(request, response, filterChain);
        verify(request, response, filterChain, sysCtx, ctx, hm, workspace, session, parentNode);
    }

    /**
    public void testActivateShouldUpdateNodeIfItAlreadyExists() {
        fail();
    }

    public void testActivateShouldCreateNodeInNewLocationAndRemoveOldOneIfItHasBeenMoved() {
        fail();
    }

    public void testCantActivateInLockedNode() {
        fail();
    }

    public void testCanUseAuthorizationOrUserId() {
        fail();
    }
    */

    /**
     * A subclass of Document which ensures we don't do anything else with it
     * than reading its stream.
     */
    private static class StreamOnlyDocument extends Document {
        private final InputStream stream;

        public StreamOnlyDocument(InputStream stream) {
            super(new File("oh no no no .txt"), null);
            this.stream = stream;
        }

        // setAtomName("blah.xml");

        public String getType() {
            throw new IllegalStateException();
        }

        public String getAtomName() {
            throw new IllegalStateException();
        }

        public String getFileName() {
            throw new IllegalStateException();
        }

        public String getFileNameWithExtension() {
            throw new IllegalStateException();
        }

        public String getExtension() {
            throw new IllegalStateException();
        }

        public long getLength() {
            throw new IllegalStateException();
        }

        public File getFile() {
            throw new IllegalStateException();
        }

        public InputStream getStream() {
            return stream;
        }
    }
}
