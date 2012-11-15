/**
 * This file Copyright (c) 2008-2012 Magnolia International
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


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.createStrictMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.isNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.startsWith;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import info.magnolia.cms.beans.runtime.Document;
import info.magnolia.cms.beans.runtime.MultipartForm;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.exchange.ActivationManager;
import info.magnolia.cms.exchange.ExchangeException;
import info.magnolia.cms.filters.WebContainerResources;
import info.magnolia.cms.filters.WebContainerResourcesImpl;
import info.magnolia.cms.security.MgnlKeyPair;
import info.magnolia.cms.security.SecurityUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.context.WebContext;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockContent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.GZIPInputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.observation.ObservationManager;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.jdom.Element;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

/**
 * Basic test for receiving end of the activation.
 * @version $Id$
 */
public class ReceiveFilterTest extends MgnlTestCase {
    private static final String PARENT_PATH = "/foo/bar";

    private static final String PRIVATE_KEY = "30820154020100300D06092A864886F70D01010105000482013E3082013A020100024100BC47ED74F1097AE7F8B55196E1874258A05B5A04AD26BA774E3EF62BE778CFA60E86B33DA31989EC5E58B261EE266DF9FF37E11D3A7C0E648198B2B06A94C58B02030100010240355BB90AF42878A1771583BADBCD665B118EF212F3334F92F224DBC51383646D31ADE8D59D04653669B2802B8FE9A7808BC97FCEFB792D63686AAB4648357DC9022100DCC291095A9142DCF80F59C5DEBE711DDFBC61C6553351D98D8A6D19436CBDBD022100DA5617DF8B736B88A75F868902A3C64D5D229988FE9DAC0932DA2220553CD0E702203DC56B93F475A501F39F47FD68005DE2801254418CE1994B88A16D399E7634F902207FF3EA63B86EB8BB4A13425DB2ED55BE6AF166F710F84824CFE7640E7CC57A4B022100C4C5FCAE8D0AAC5D876CAED92A34E19C5B12A965EA7DFB3A7854D6652A16B2F2";
    private static final String PUBLIC_KEY = "305C300D06092A864886F70D0101010500034B003048024100BC47ED74F1097AE7F8B55196E1874258A05B5A04AD26BA774E3EF62BE778CFA60E86B33DA31989EC5E58B261EE266DF9FF37E11D3A7C0E648198B2B06A94C58B0203010001";

    private static interface TestCallBack {

        Content getParentNode();

        void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception;

        void checkPermissions(HierarchyManager hm);

        void checkNode(HierarchyManager hm) throws Exception;

        void checkParent(HierarchyManager hm, boolean wasLocked) throws Exception;

        void importNode(HierarchyManager hm, Session session) throws Exception;

        void saveSession(HierarchyManager hm) throws Exception;
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ComponentsTestUtil.setImplementation(WebContainerResources.class, WebContainerResourcesImpl.class);
        // public key retrieval
        ActivationManager actMan = mock(ActivationManager.class);
        ComponentsTestUtil.setInstance(ActivationManager.class, actMan);
        when(actMan.getPublicKey()).thenReturn(PUBLIC_KEY);

    }

    @Override
    @After
    public void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    @Test
    public void testActivateShouldCreateNewNodeIfItDoesNotExist() throws Exception {
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void checkNode(HierarchyManager hm) throws Exception {
                //before
                expect(hm.getContentByUUID("DUMMY-UUID")).andThrow(new ItemNotFoundException());
                expect(hm.isExist("/foo/bar/nodename")).andReturn(false);
                // after it have been created already
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(new MockContent("blah"));
            }

            @Override
            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {
                session.importXML(eq(PARENT_PATH), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_COLLISION_REMOVE_EXISTING));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    @Override
                    public Object answer() throws Throwable {
                        final InputStream passedStream = (InputStream) getCurrentArguments()[1];
                        final InputStream expectedStream = new GZIPInputStream(getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz"));
                        assertTrue("Tried to import an unexpected stream", IOUtils.contentEquals(expectedStream, passedStream));
                        return null;
                    }
                });
            }
        }, true);
    }

    @Test
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
        expect(tempNode.getContent("nodename")).andReturn(importedNode);

        // for the sake of this test we'll just pretend we have no properties on the imported node either
        expect(importedNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        expect(root.getContent("activation-tmp")).andReturn(activationTmp).anyTimes();
        // creating temp node. The node is created with UUID of the parent as a name, so can't really get the name here. Later on we pretend the name (and therefore the handle) is DUMMY-UUID
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:page"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(ctx.getHierarchyManager("some-workspace")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            @Override
            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            @Override
            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(startsWith("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    @Override
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
        }, true);
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
        expect(tempNode.getContent("nodename")).andReturn(importedNode);

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
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:page"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            @Override
            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            @Override
            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(eq("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    @Override
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
        }, true);
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
        expect(tempNode.getContent("nodename")).andReturn(importedNode);

        // for the sake of this test we'll just pretend we have no properties on the imported node
        expect(importedNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        // TODO : why are properties copied using the jcr api ??
        expect(existingNode.getJCRNode()).andReturn(null);

        // for the sake of this test we'll just pretend we have no properties on the imported node either
        expect(existingNode.getNodeDataCollection()).andReturn(Collections.<NodeData>emptyList());

        expect(root.getContent("activation-tmp")).andReturn(activationTmp).anyTimes();
        // creating temp node. The node is created with UUID of the parent as a name, so can't really get the name here. Later on we pretend the name (and therefore the handle) is DUMMY-UUID
        expect(activationTmp.createContent(isA(String.class), eq("mgnl:page"))).andReturn(tempNode);

        Object[] mocks = new Object[] {existingNode, tempNode, importedNode, existingParent, root, activationTmp};
        replay(mocks);
        doTest("activate", "sa_success", "", new AbstractTestCallBack() {

            @Override
            public void createTemp(SystemContext ctx, HierarchyManager hm) throws Exception {
                expect(ctx.getHierarchyManager("mgnlSystem")).andReturn(hm).anyTimes();
                expect(hm.getRoot()).andReturn(root);
            }

            @Override
            public void checkNode(HierarchyManager hm) throws Exception {
                expect(hm.getContentByUUID("DUMMY-UUID")).andReturn(existingNode);
            }

            @Override
            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {

                session.importXML(eq("/DUMMY-UUID"), isA(InputStream.class), eq(ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW));
                expectLastCall().andAnswer(new IAnswer<Object>() {
                    @Override
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
        }, true);
        verify(mocks);
    }

    @Test
    public void testCantActivateInLockedNode() throws Exception {
        // use a nice mock as because of multithreading we are not able to specify exact order and number of calls
        final Content parentNode = createNiceMock(Content.class);
        final Content existingNode = createStrictMock(Content.class);
        final Content tempNode = createStrictMock(Content.class);
        replay(existingNode, tempNode);
        doTest("activate", "sa_failed", "Content /foo/bar was locked while activating some-uuid. This most likely means that content have been at the same time activated by some other user. Please try again and if problem persists contact administrator.", new AbstractTestCallBack() {

            @Override
            public Content getParentNode() {
                return parentNode;
            }

            @Override
            public void checkParent(HierarchyManager hm, boolean wasLocked) throws Exception {
                expect(hm.getContent(PARENT_PATH)).andReturn(parentNode).anyTimes();
                expect(hm.getContent(PARENT_PATH + "/")).andReturn(parentNode).anyTimes();
                // check the lock
                expect(parentNode.isLocked()).andReturn(true).anyTimes();
                // create exception message
                expect(parentNode.getHandle()).andReturn(PARENT_PATH).anyTimes();

                if (wasLocked) {
                    // clean up ... check the lock again
                    //expect(parentNode.isLocked()).andReturn(true).times(2);
                    // try to unlock ... TODO: is that a right thing to do ... we are not the ones who locked it here
                    parentNode.unlock();
                }
            }

            @Override
            public void checkNode(HierarchyManager hm) throws Exception {
                // won't be called as parent is locked
            }

            @Override
            public void saveSession(HierarchyManager hm) throws Exception {
                // don't save ... we've not activated anything
            }

            @Override
            public void importNode(HierarchyManager hm, Session session) throws IOException, RepositoryException {
                // won't be called as parent is locked
            }

        }, false);
        verify(existingNode, tempNode);
    }
    /*
    public void testCanUseAuthorizationOrUserId() {
        fail();
    }
     */

    private void doTest(final String action, final String expectedStatus, final String expectedMessage, TestCallBack testCallBack, boolean wasLocked) throws Exception {        final HttpServletRequest request = createMock(HttpServletRequest.class); // not strict: we don't want to check method call order
    final HttpServletResponse response = createStrictMock(HttpServletResponse.class);
    final FilterChain filterChain = createStrictMock(FilterChain.class);
    final SystemContext sysCtx = createStrictMock(SystemContext.class);
    final WebContext ctx = createMock(WebContext.class);
    final HierarchyManager hm = createMock(HierarchyManager.class);
    final Workspace workspace = createStrictMock(Workspace.class);
    final Session session = createStrictMock(Session.class);

    Node keyNode = mock(Node.class);
    PropertyIterator propIter = mock(PropertyIterator.class);
    NodeIterator nodeIter = mock(NodeIterator.class);
    Property keyProp = mock(Property.class);
    when(keyNode.getProperties()).thenReturn(propIter);
    when(propIter.hasNext()).thenReturn(true).thenReturn(false);
    when(propIter.next()).thenReturn(keyProp);
    when(keyProp.getName()).thenReturn("publicKey");
    when(keyNode.getNodes()).thenReturn(nodeIter);
    ObservationManager obsMan = mock(ObservationManager.class);

    final InputStream xmlStream = getClass().getResourceAsStream("/resources4189.xml");
    final InputStream nodeStream = getClass().getResourceAsStream("/exchange_threadReply4173.xml.gz");
    assertNotNull(xmlStream);
    assertNotNull(nodeStream);
    final MultipartForm form = new MultipartForm();
    // the file name (documents key) is only used here as the key of the map, is unrelated to actual name.
    form.getDocuments().put("blah.xml", new StreamOnlyDocument(xmlStream));
    form.getDocuments().put("node-to-activate.xml.gz", new StreamOnlyDocument(nodeStream));

    expect(session.hasPermission(isA(String.class), eq(Session.ACTION_ADD_NODE))).andReturn(true).anyTimes();

    ComponentsTestUtil.setInstance(SystemContext.class, sysCtx);
    MgnlContext.setInstance(ctx);

    // we verify timestamp is not too old so we need to have fresh one for test as well
    String message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;14D600CAB64A608D5D2780E00A4A6CA2", PRIVATE_KEY);
    // RF will fail silently on empty message since it might be just random hit to given uri.
    assertFalse(message.isEmpty());
    expect(request.getHeader("X-magnolia-act-auth")).andReturn(message).anyTimes();

    // checking headers
    expect(request.getHeader("mgnlUTF8Status")).andReturn("true").anyTimes();
    expect(request.getHeader("mgnlExchangeAction")).andReturn(action).anyTimes();
    expect(request.getHeader("mgnlExchangeParentPath")).andReturn(PARENT_PATH).anyTimes();
    expect(request.getHeader(BaseSyndicatorImpl.NODE_UUID)).andReturn("some-uuid").anyTimes();
    expect(request.getHeader("mgnlExchangeRepositoryName")).andReturn("some-repo").anyTimes();
    expect(request.getHeader("mgnlExchangeWorkspaceName")).andReturn("some-workspace").anyTimes();
    expect(request.getHeader("mgnlExchangeResourceMappingFile")).andReturn("blah.xml").anyTimes(); // this is hardcoded to resources.xml in BaseSyndicatorImpl
    // TODO : check if different rules are passed in different cases ?
    expect(request.getHeader("mgnlExchangeFilterRule")).andReturn("mgnl:page,mgnl:metaData,mgnl:resource,").anyTimes(); // this is hardcoded to resources.xml in BaseSyndicatorImpl
    expect(request.getHeader("Authorization")).andReturn(null).anyTimes();
    expect(request.getSession()).andReturn(null).anyTimes();
    expect(request.getParameter("mgnlUserId")).andReturn("testuser").anyTimes();
    expect(request.getAttribute(EasyMock.<String>anyObject())).andReturn(null).anyTimes();

    // checking parent node
    testCallBack.checkParent(hm, wasLocked);

    expect(ctx.getHierarchyManager("some-workspace")).andReturn(hm).anyTimes();
    expect(sysCtx.getHierarchyManager("some-workspace")).andReturn(hm).anyTimes();
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

    if (wasLocked) {
        //cleanup()
        expect(hm.isExist("/foo/bar")).andReturn(true);
        expect(request.getSession(false)).andReturn(null);
    }

    final ReceiveFilter filter = new ReceiveFilter(new ExchangeSimpleModule());
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

        @Override
        public String getType() {
            throw new IllegalStateException();
        }

        @Override
        public String getAtomName() {
            throw new IllegalStateException();
        }

        @Override
        public String getFileName() {
            throw new IllegalStateException();
        }

        @Override
        public String getFileNameWithExtension() {
            throw new IllegalStateException();
        }

        @Override
        public String getExtension() {
            throw new IllegalStateException();
        }

        @Override
        public long getLength() {
            throw new IllegalStateException();
        }

        @Override
        public File getFile() {
            throw new IllegalStateException();
        }

        @Override
        public InputStream getStream() {
            return stream;
        }
    }

    /**
     * Callback for test.
     */
    public abstract class AbstractTestCallBack implements TestCallBack {
        private final Content parentNode = createMock(Content.class); // TODO this should maybe be strict

        @Override
        public void checkParent(HierarchyManager hm, boolean wasLocked) throws Exception {
            expect(hm.getContent(PARENT_PATH)).andReturn(parentNode).anyTimes();
            expect(hm.getContent(PARENT_PATH + "/")).andReturn(parentNode).anyTimes();
            // order last child
            parentNode.orderBefore(isA(String.class), (String) isNull());
            parentNode.save();
            //cleanup
            expect(parentNode.isLocked()).andReturn(false).anyTimes();
            expect(parentNode.lock(true, true)).andReturn(null);
        }

        @Override
        public void checkPermissions(HierarchyManager hm) {
            // do nothing by default
        }

        @Override
        public Content getParentNode() {
            return parentNode;
        }

        @Override
        public void saveSession(HierarchyManager hm) throws Exception {
            hm.save();
        }

        @Override
        public void createTemp(SystemContext arg0, HierarchyManager arg1) throws Exception {
            // nothing by default
        }
    }

    /**
     * Tests the decryption, time check and parsing of the encrypted message
     * 
     * @throws Exception
     */
    @Test
    public void testReceive() throws Exception {
        ExchangeSimpleModule module = mock(ExchangeSimpleModule.class);
        // one sec delay tolerance on receive
        when(module.getActivationDelayTolerance()).thenReturn(1000L);
        HttpServletRequest req = mock(HttpServletRequest.class);
        final String md5 = "C750AFBA94E355BF5544434E227708C3";
        ReceiveFilter filter = new ReceiveFilter(module) {

            @Override
            public synchronized String receive(HttpServletRequest request) throws Exception {
                // widen visibility for testing purposes
                return super.receive(request);
            }

            @Override
            protected synchronized String update(HttpServletRequest request, String resourcesmd5) throws Exception {
                // do nothing in test
                assertEquals(md5, resourcesmd5);
                return "/bla";
            }
        };
        String message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;" + md5, PRIVATE_KEY);
        // RF will fail silently on empty message since it might be just random hit to given uri.
        assertFalse(message.isEmpty());
        when(req.getHeader("X-magnolia-act-auth")).thenReturn(message);
        when(req.getHeader(BaseSyndicatorImpl.ACTION)).thenReturn(BaseSyndicatorImpl.ACTIVATE);
        filter.receive(req);

        // and again, but too late (5sec)
        message = SecurityUtil.encrypt((System.currentTimeMillis() - 5000) + ";johndoe;" + md5, PRIVATE_KEY);
        // RF will fail silently on empty message since it might be just random hit to given uri.
        assertFalse(message.isEmpty());
        when(req.getHeader("X-magnolia-act-auth")).thenReturn(message);
        when(req.getHeader(BaseSyndicatorImpl.ACTION)).thenReturn(BaseSyndicatorImpl.ACTIVATE);
        try {
            filter.receive(req);
            fail("Replay of old act messages should not be tolerated.");
        } catch (SecurityException e) {
            assertEquals("Activation refused due to request arriving too late or time not synched between author and public instance. Please contact your administrator to ensure server times are synced or the tolerance is set high enough to counter the differences.", e.getMessage());
        }

        // we attempt to log where bogus messages come from
        when(req.getRemoteAddr()).thenReturn("evilMan");
        // and again with fake message
        message = "AAAF065B07FD670CFAB87D9BDA49F937C82270B8F7D6191D30EC4141434AB4B041EEECC699BFDEDFE2A4448880E2D140D1BF51697CE2699DFCCC749B3317BE78";
        // RF will fail silently on empty message since it might be just random hit to given uri.
        assertFalse(message.isEmpty());
        when(req.getHeader("X-magnolia-act-auth")).thenReturn(message);
        when(req.getHeader(BaseSyndicatorImpl.ACTION)).thenReturn(BaseSyndicatorImpl.ACTIVATE);
        try {
            filter.receive(req);
            fail("Fake signature should not be tolerated.");
        } catch (SecurityException e) {
            assertEquals("Handshake information for activation was incorrect. Someone attempted to impersonate author instance. Incoming request was from evilMan", e.getMessage());
        }
    }

    @Test
    public void testUpdate() throws Exception {
        ExchangeSimpleModule module = mock(ExchangeSimpleModule.class);
        // one sec delay tolerance on receive
        when(module.getActivationDelayTolerance()).thenReturn(1000L);
        HttpServletRequest req = mock(HttpServletRequest.class);

        final HierarchyManager hierarchyManager = mock(HierarchyManager.class);
        ReceiveFilter filter = new ReceiveFilter(module) {

            @Override
            public synchronized String update(HttpServletRequest request, String resourcesmd5) throws Exception {
                // widen visibility for testing
                return super.update(request, resourcesmd5);
            }

            @Override
            protected String getParentPath(HttpServletRequest request) {
                return "/pooh";
            }

            @Override
            protected HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
                return hierarchyManager;
            }

            @Override
            protected synchronized void importOnExisting(Element topContentElement, MultipartForm data, HierarchyManager hierarchyManager, Content existingContent) throws ExchangeException, RepositoryException {
                // not testing import yet
            }
        };

        WebContext ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);
        MultipartForm form = mock(MultipartForm.class);
        when(ctx.getPostedForm()).thenReturn(form);
        InputStream xmlStream = getClass().getResourceAsStream("/resources4189.xml");
        assertNotNull(xmlStream);
        when(form.getDocument("aResourceDoc.xml")).thenReturn(new StreamOnlyDocument(xmlStream));
        when(req.getHeader(BaseSyndicatorImpl.RESOURCE_MAPPING_FILE)).thenReturn("aResourceDoc.xml");

        // fake md5 => fail with ex
        String md5 = "db99f7630853825dabbc17f093d2236e";
        try {
            filter.update(req, md5);
            fail("Incorrect MD5 should not be tolerated.");
        } catch (SecurityException e) {
            assertEquals("Signature of received resource (14D600CAB64A608D5D2780E00A4A6CA2) doesn't match expected signature (db99f7630853825dabbc17f093d2236e). This might mean that the activation operation have been intercepted by a third party and content have been modified during transfer.", e.getMessage());
        }

        // ensure we close used resources
        try {
            xmlStream.read();
            fail("Stream should be closed at this point");
        } catch (IOException e) {
            // stream is closed
        }

        // correctmd5
        md5 = "14D600CAB64A608D5D2780E00A4A6CA2";
        // stream is closed after import so to run again we have to recreate it
        xmlStream = getClass().getResourceAsStream("/resources4189.xml");
        assertNotNull(xmlStream);
        when(form.getDocument("aResourceDoc.xml")).thenReturn(new StreamOnlyDocument(xmlStream));

        Content poohBarContent = mock(Content.class);
        when(hierarchyManager.getContentByUUID("DUMMY-UUID")).thenReturn(poohBarContent);
        when(poohBarContent.getHandle()).thenReturn("/pooh/bar");
        when(poohBarContent.getName()).thenReturn("bar");
        when(req.getHeader(BaseSyndicatorImpl.CONTENT_FILTER_RULE)).thenReturn("mgnl:content,mgnl:resource");

        Content poohContent = mock(Content.class);
        when(hierarchyManager.getContent("/pooh/")).thenReturn(poohContent);

        filter.update(req, md5);

    }

    @Test
    public void testRemove() throws Exception {
        ExchangeSimpleModule module = mock(ExchangeSimpleModule.class);
        // one sec delay tolerance on receive
        when(module.getActivationDelayTolerance()).thenReturn(1000L);
        HttpServletRequest req = mock(HttpServletRequest.class);

        final HierarchyManager hierarchyManager = mock(HierarchyManager.class);
        ReceiveFilter filter = new ReceiveFilter(module) {

            @Override
            public synchronized String remove(HttpServletRequest request, String md5) throws Exception {
                // widening visibility for tests
                return super.remove(request, md5);
            }

            @Override
            protected HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
                return hierarchyManager;
            }

        };

        WebContext ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);
        // String message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;61055CD40D31760E317789A3159E548D", PRIVATE_KEY);
        // when(req.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH)).thenReturn(message);
        when(req.getHeader(BaseSyndicatorImpl.NODE_UUID)).thenReturn("SOME-FAKE-UUID");
        Content someContent = mock(Content.class);
        when(hierarchyManager.getContentByUUID("SOME-FAKE-UUID")).thenReturn(someContent);
        when(someContent.getHandle()).thenReturn("/bla/boo");
        filter.remove(req, "61055CD40D31760E317789A3159E548D");
        verify(hierarchyManager).delete("/bla/boo");

        // try to fake the sig ... and fail
        // message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;C750AFBA94E355BF5544434E227708C3", PRIVATE_KEY);
        // when(req.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH)).thenReturn(message);
        when(req.getRemoteAddr()).thenReturn("evilMan");
        try {
            filter.remove(req, "C750AFBA94E355BF5544434E227708C3");
            fail("Attempt to delete content with invalid signature should fail.");
        } catch (SecurityException e) {
            assertEquals("Signature of resource doesn't match. This seems like malicious attempt to delete content. Request was issued from evilMan", e.getMessage());

        }
    }

    @Test
    public void testEstablishTrustOnDeactivate() throws Exception {
        ExchangeSimpleModule module = mock(ExchangeSimpleModule.class);
        // one sec delay tolerance on receive
        when(module.getActivationDelayTolerance()).thenReturn(100000L);
        when(module.getActivationKeyLength()).thenReturn(512);
        HttpServletRequest req = mock(HttpServletRequest.class);

        // - emulate no public key yet
        ActivationManager actMan = mock(ActivationManager.class);
        ComponentsTestUtil.setInstance(ActivationManager.class, actMan);
        when(actMan.getPublicKey()).thenReturn(null);

        final HierarchyManager hierarchyManager = mock(HierarchyManager.class);
        ReceiveFilter filter = new ReceiveFilter(module) {

            @Override
            protected HierarchyManager getHierarchyManager(HttpServletRequest request) throws ExchangeException {
                return hierarchyManager;
            }

            @Override
            protected void applyLock(HttpServletRequest request) throws ExchangeException {
                // ignore locking in this test
            }

            @Override
            protected synchronized String remove(HttpServletRequest request, String md5) throws Exception {
                // ignore removal itself as well (tested in testRemove() )
                return "/some/path";
            }
        };

        WebContext ctx = mock(WebContext.class);
        MgnlContext.setInstance(ctx);
        when(req.getHeader(BaseSyndicatorImpl.NODE_UUID)).thenReturn("SOME-FAKE-UUID");
        when(req.getHeader(BaseSyndicatorImpl.ACTION)).thenReturn(BaseSyndicatorImpl.DEACTIVATE);

        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(req, res, chain);
        // get handshake on first attempt
        verify(res).setHeader(BaseSyndicatorImpl.ACTIVATION_ATTRIBUTE_STATUS, BaseSyndicatorImpl.ACTIVATION_HANDSHAKE);

        // bit of mockito voodoo ... we just need to learn what temp keys are, not really match them against anything
        final String[] tempKeys = new String[2];
        verify(module).setTempKeys((MgnlKeyPair) argThat(new ArgumentMatcher() {

            @Override
            public boolean matches(Object argument) {
                MgnlKeyPair keys = (MgnlKeyPair) argument;
                tempKeys[0] = keys.getPrivateKey();
                tempKeys[1] = keys.getPublicKey();
                return true;
            }
        }));

        // String message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;61055CD40D31760E317789A3159E548D", PRIVATE_KEY);
        assertNotNull(tempKeys[0]);
        when(req.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH_KEY)).thenReturn(SecurityUtil.encrypt(PUBLIC_KEY, tempKeys[1]));
        String message = SecurityUtil.encrypt(System.currentTimeMillis() + ";johndoe;61055CD40D31760E317789A3159E548D", PRIVATE_KEY);
        when(req.getHeader(BaseSyndicatorImpl.ACTIVATION_AUTH)).thenReturn(message);
        when(module.getTempKeys()).thenReturn(new MgnlKeyPair(tempKeys[0], tempKeys[1]));
        // store public key
        SystemContext sysctx = mock(SystemContext.class);
        ComponentsTestUtil.setInstance(SystemContext.class, sysctx);
        Session sysSession = mock(Session.class);
        when(sysctx.getJCRSession("config")).thenReturn(sysSession);
        Node actNode = mock(Node.class);
        when(sysSession.getNode("/server/activation")).thenReturn(actNode);
        // we operate on mocks so we assume here update was correct and verify that session.save() was called after update
        // ... the below says to mockito to return null on first call to the method and public key on all calls thereafter
        when(actMan.getPublicKey()).thenReturn(null).thenReturn(PUBLIC_KEY);
        filter.doFilter(req, res, chain);
        // get success after handshake
        verify(sysSession).save();
        verify(res).setHeader(BaseSyndicatorImpl.ACTIVATION_ATTRIBUTE_STATUS, BaseSyndicatorImpl.ACTIVATION_SUCCESSFUL);

    }
}
