/**
 * This file Copyright (c) 2008-2010 Magnolia International
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
package info.magnolia.module.exchangesimple;

import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.mock.MockContent;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import static org.easymock.EasyMock.*;
import org.easymock.IAnswer;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class ReceiveFilterTest extends TestCase {
    private static final String PARENT_PATH = "/foo/bar";

    private static interface TestCallBack {

        Content getParentNode();

        void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception;

        void checkPermissions(HierarchyManager hm);

        void checkNode(HierarchyManager hm) throws Exception;

        void checkParent(HierarchyManager hm) throws Exception;

        void importNode(HierarchyManager hm, Session session) throws Exception;

        void saveSession(HierarchyManager hm) throws Exception;
    }

    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    public void testActivateShouldCreateNewNodeIfItDoesNotExist() throws Exception {
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {
            public void checkPermissions(HierarchyManager hm) {
                // TODO : really, really, this should just crash
                //  - and we should actually check permissions and assert the behaviour of ReceiveFilter when the user does not have the appropriate permissions
                expect(hm.getAccessManager()).andReturn(null);

            }

            public void checkNode(HierarchyManager hm) throws Exception {
                //before
                expect(hm.getContentByUUID("DUMMY-UUID")).andThrow(new ItemNotFoundException());
                expect(hm.isExist("/foo/bar/nodename")).andReturn(false);
                // after it have been created already
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(new MockContent("blah"));
            }

            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {
                session.importXML(eq(PARENT_PATH), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    public Object answer() throws Throwable {
                        final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                        final InputStream expectedStream = new GZIPInputStream(getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz"));
                        assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                        return null;
                    }
                });
            }
        });
    }

    public void testActivateShouldUpdateNodeIfItAlreadyExists() throws Exception {
        final Content existingNode = createMock(Content.class); // can't make it strict, as getHandle and getName are called plenty of times
        final Content existingParent = createStrictMock(Content.class);
        final Content tempNode = createStrictMock(Content.class);
        final Content importedNode = createStrictMock(Content.class);
        final Content root = createStrictMock(Content.class);
        final Content activationTmp = createStrictMock(Content.class);

        expect(existingNode.getHandle()).andReturn(PARENT_PATH + "/nodename").anyTimes();
        expect(existingNode.getName()).andReturn("nodename");
        // TODO : test when existing node has children ?
        expect(existingNode.getChildren(isA(Content.ContentFilter.class))).andReturn(Collections.<Content>emptyList());

//        expect(existingNode.getParent()).andReturn(existingParent);
//        expect(existingParent.getHandle()).andReturn("/");

        // for the sake of this test we'll just pretend we have no properties on the existing node
        expect(existingNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        // TODO : why are properties copied using the jcr api ??
        expect(existingNode.getJCRNode()).andReturn(null);

        // get temp node handle
        expect(tempNode.getHandle()).andReturn("/DUMMY-UUID");

        // get node with imported properties
        expect(tempNode.getChildByName("nodename")).andReturn(importedNode);

        // for the sake of this test we'll just pretend we have no properties on the imported node either
        expect(importedNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        expect(root.getContent("activation-tmp")).andReturn(activationTmp).anyTimes();
        // creating temp node. The node is created with UUID of the parent as a name, so can't really get the name here. Later on we pretend the name (and therefore the handle) is DUMMY-UUID
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:contentNode"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(startsWith("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    public Object answer() throws Throwable {
                        final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                        final InputStream expectedStream = new GZIPInputStream(getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz"));
                        assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                        return null;
                    }
                });

                hm.delete(startsWith("/DUMMY-UUID"));
            }

            @Override
            public void saveSession(HierarchyManager hm) throws Exception {
                super.saveSession(hm);
                // save after deleting the temp node
                hm.save();
            }
        });
        verify(mocks);
    }

    public void testActivateShouldMoveToNewLocationIfItHasBeenMovedToADifferentPath() throws Exception {
        final Content existingNode = createMock(Content.class); // can't make it strict, as getHandle and getName are called plenty of times
        final Content existingParent = createStrictMock(Content.class);
        final Content tempNode = createStrictMock(Content.class);
        final Content importedNode = createStrictMock(Content.class);
        final Content root = createStrictMock(Content.class);
        final Content activationTmp = createStrictMock(Content.class);

        expect(existingNode.getHandle()).andReturn(PARENT_PATH + "/oldnodename").anyTimes();
        expect(existingNode.getName()).andReturn("oldnodename");
        // TODO : test when existing node has children ?
        expect(existingNode.getChildren(isA(Content.ContentFilter.class))).andReturn(Collections.<Content>emptyList());

        // get temp node handle
        expect(tempNode.getHandle()).andReturn("/DUMMY-UUID");

        // get node with imported properties
        expect(tempNode.getChildByName("nodename")).andReturn(importedNode);

        // for the sake of this test we'll just pretend we have no properties on the imported node
        expect(importedNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        //expect(existingNode.getParent()).andReturn(existingParent);
        //expect(existingParent.getHandle()).andReturn("/");

        // for the sake of this test we'll just pretend we have no properties on the existing node either
        expect(existingNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        // TODO : why are properties copied using the jcr api ??
        expect(existingNode.getJCRNode()).andReturn(null);

        expect(root.getContent("activation-tmp")).andReturn(activationTmp).anyTimes();
        // creating temp node. The node is created with UUID of the parent as a name, so can't really get the name here. Later on we pretend the name (and therefore the handle) is DUMMY-UUID
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:contentNode"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(eq("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    public Object answer() throws Throwable {
                        final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                        final InputStream expectedStream = new GZIPInputStream(getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz"));
                        assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                        return null;
                    }
                });

                hm.moveTo(PARENT_PATH + "/oldnodename", PARENT_PATH + "/nodename");
                hm.delete("/DUMMY-UUID");
            }

            @Override
            public void saveSession(HierarchyManager hm) throws Exception {
                super.saveSession(hm);
                // save after deleting the temp node
                hm.save();
            }
        });
        verify(mocks);
    }

    public void testActivateShouldMoveWhenParentHasChanged() throws Exception {
        final Content existingNode = createMock(Content.class); // can't make it strict, as getHandle and getName are called plenty of times
        final Content existingParent = createStrictMock(Content.class);
        final Content tempNode = createStrictMock(Content.class);
        final Content importedNode = createStrictMock(Content.class);
        final Content root = createStrictMock(Content.class);
        final Content activationTmp = createStrictMock(Content.class);

        expect(existingNode.getHandle()).andReturn(PARENT_PATH + "old/nodename").anyTimes();
        // TODO : test when existing node has children ?
        expect(existingNode.getChildren(isA(Content.ContentFilter.class))).andReturn(Collections.<Content>emptyList());

        // get temp node handle
        expect(tempNode.getHandle()).andReturn("/DUMMY-UUID");

        // get node with imported properties
        expect(tempNode.getChildByName("nodename")).andReturn(importedNode);

        // for the sake of this test we'll just pretend we have no properties on the imported node
        expect(importedNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

//        expect(existingNode.getParent()).andReturn(existingParent);
//        expect(existingParent.getHandle()).andReturn("/");

        // TODO : why are properties copied using the jcr api ??
        expect(existingNode.getJCRNode()).andReturn(null);

        // for the sake of this test we'll just pretend we have no properties on the imported node either
        expect(existingNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        expect(root.getContent("activation-tmp")).andReturn(activationTmp).anyTimes();
        // creating temp node. The node is created with UUID of the parent as a name, so can't really get the name here. Later on we pretend the name (and therefore the handle) is DUMMY-UUID
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:contentNode"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(eq("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    public Object answer() throws Throwable {
                        final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                        final InputStream expectedStream = new GZIPInputStream(getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz"));
                        assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                        return null;
                    }
                });

                hm.moveTo(PARENT_PATH + "old/nodename", PARENT_PATH + "/nodename");
                hm.delete("/DUMMY-UUID");
            }

            @Override
            public void saveSession(HierarchyManager hm) throws Exception {
                super.saveSession(hm);
                // save after deleting the temp node
                hm.save();
            }
        });
        verify(mocks);
    }


    public void testCantActivateInLockedNode() throws Exception {
        // use a nice mock as because of multithreading we are not able to specify exact order and number of calls
        final Content parentNode = createNiceMock(Content.class);
        final Content existingNode = createStrictMock(Content.class);
        final Content tempNode = createStrictMock(Content.class);
        replay(existingNode, tempNode);
        doTest("activate", "sa_failed", "Operation not permitted, /foo/bar is locked", new AbstractTestCallBack() {

            public Content getParentNode() {
                return parentNode;
            }

            public void checkParent(HierarchyManager hm) throws Exception {
                expect(hm.getContent(PARENT_PATH)).andReturn(parentNode).anyTimes();
                expect(hm.getContent(PARENT_PATH + "/")).andReturn(parentNode).anyTimes();
                // check the lock
                expect(parentNode.isLocked()).andReturn(true).anyTimes();
                // create exception message
                expect(parentNode.getHandle()).andReturn(PARENT_PATH).anyTimes();

                // clean up ... check the lock again
                //expect(parentNode.isLocked()).andReturn(true).times(2);
                // try to unlock ... TODO: is that a right thing to do ... we are not the ones who locked it here
                parentNode.unlock();
            }

            public void checkNode(HierarchyManager hm) throws Exception {
                // won't be called as parent is locked
            }

            public void saveSession(HierarchyManager hm) throws Exception {
                // don't save ... we've not activated anything
            }

            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {
                // won't be called as parent is locked
            }

        });
        verify(existingNode, tempNode);
    }
/*
    public void testCanUseAuthorizationOrUserId() {
        fail();
    }
    */

    private void doTest(final String action, final String expectedStatus, final String expectedMessage, TestCallBack testCallBack) throws Exception {
        final HttpServletRequest request = createMock(HttpServletRequest.class); // not strict: we don't want to check method call order
        final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
        final FilterChain filterChain = createStrictMock(FilterChain.class);
        final SystemContext sysCtx = createStrictMock(SystemContext.class);
        final WebContext ctx = createMock(WebContext.class);
        final HierarchyManager hm = createMock(HierarchyManager.class);
        final Workspace workspace = createStrictMock(Workspace.class);
        final Session session = createStrictMock(Session.class);

        final InputStream xmlStream = getClass().getResourceAsStream("/resources4189.xml");
        final InputStream nodeStream = getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz");
        assertNotNull(xmlStream);
        assertNotNull(nodeStream);
        final MultipartForm form = new MultipartForm();
        // the file name (documents key) is only used here as the key of the map, is unrelated to actual name.
        form.getDocuments().put("blah.xml", new StreamOnlyDocument(xmlStream));
        form.getDocuments().put("node-to-activate.xml.gz", new StreamOnlyDocument(nodeStream));

        ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);
        MgnlContext.setInstance(ctx);

        // checking headers
        expect(request.getHeader("mgnlExchangeAction")).andReturn(action).anyTimes();
        expect(request.getHeader("mgnlExchangeParentPath")).andReturn(PARENT_PATH).anyTimes();
        expect(request.getHeader("mgnlExchangeRepositoryName")).andReturn("some-repo").anyTimes();
        expect(request.getHeader("mgnlExchangeWorkspaceName")).andReturn("some-workspace").anyTimes();
        expect(request.getHeader("mgnlExchangeResourceMappingFile")).andReturn("blah.xml").anyTimes(); // this is hardcoded to resources.xml in BaseSyndicatorImpl
        // TODO : check if different rules are passed in different cases ?
        expect(request.getHeader("mgnlExchangeFilterRule")).andReturn("mgnl:contentNode,mgnl:metaData,mgnl:resource,").anyTimes(); // this is hardcoded to resources.xml in BaseSyndicatorImpl
        expect(request.getHeader("Authorization")).andReturn(null).anyTimes();
        expect(request.getSession()).andReturn(null).anyTimes();
        expect(request.getParameter("mgnlUserId")).andReturn("testuser").anyTimes();

        // checking parent node
        testCallBack.checkParent(hm);

        expect(ctx.getHierarchyManager("some-repo", "some-workspace")).andReturn(hm).anyTimes();
        expect(ctx.getPostedForm()).andReturn(form).anyTimes();

        // copying temp node
        // in reality it will be a different hm, but for a sake of the test we use the same one
        testCallBack.createTemp(sysCtx, hm);

        testCallBack.checkPermissions(hm);
        testCallBack.checkNode(hm);

        // importing node
        expect(hm.getWorkspace()).andReturn(workspace).anyTimes();
        expect(workspace.getSession()).andReturn(session).anyTimes();
        testCallBack.importNode(hm, session);
        testCallBack.saveSession(hm);

        // response
        response.setHeader("sa_attribute_status", expectedStatus);
        response.setHeader("sa_attribute_message", expectedMessage);

        //cleanup()
        expect(hm.isExist("/foo/bar")).andReturn(true);
        expect(request.getSession(false)).andReturn(null);

        final ReceiveFilter filter = new ReceiveFilter();
        filter.setUnlockRetries(1);
        Object[] mocks = new Object[] {request, response, filterChain, sysCtx, ctx, hm, workspace, session, testCallBack.getParentNode()};
        replay(mocks);
        filter.doFilter(request, response, filterChain);
        verify(mocks);
    }


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

    public abstract class AbstractTestCallBack implements TestCallBack {
        private final Content parentNode = createMock(Content.class); // TODO this should maybe be strict

        public void checkParent(HierarchyManager hm) throws Exception {
            expect(hm.getContent(PARENT_PATH)).andReturn(parentNode).anyTimes();
            expect(hm.getContent(PARENT_PATH + "/")).andReturn(parentNode).anyTimes();
            // order last child
            parentNode.orderBefore(isA(String.class), (String) isNull());
            parentNode.save();
            //cleanup
            expect(parentNode.isLocked()).andReturn(false).anyTimes();
            expect(parentNode.lock(true, true)).andReturn(null);
        }

        public void checkPermissions(HierarchyManager hm) {
            // do nothing by default
        }

        public Content getParentNode() {
            return parentNode;
        }

        public void saveSession(HierarchyManager hm) throws Exception {
            hm.save();
        }

        public void createTemp(SystemContext arg0, HierarchyManager arg1) throws Exception {
            // nothing by default
        }
    }
}
