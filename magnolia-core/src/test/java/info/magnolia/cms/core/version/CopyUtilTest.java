/**
 * This file Copyright (c) 2013 Magnolia International
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
package info.magnolia.cms.core.version;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.core.MgnlNodeType;
import info.magnolia.cms.security.Realm;
import info.magnolia.cms.security.SecuritySupport;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.BootstrapUtil;
import info.magnolia.jcr.predicate.AbstractPredicate;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.jackrabbit.commons.predicate.Predicate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for CopyUtil.
 */
public class CopyUtilTest extends RepositoryTestCase {



    private CopyUtil util;

    private Session websiteSession;
    private Session mgnlVersionSession;
    private Node srcTest;
    private Node versioned;

    private Predicate filter;
    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        // prepare


        SecuritySupport securitySupportMock = mock(SecuritySupport.class);
        ComponentsTestUtil.setInstance(SecuritySupport.class, securitySupportMock);
        UserManager userManagerMock = mock(UserManager.class);
        when(securitySupportMock.getUserManager(Realm.REALM_SYSTEM.getName())).thenReturn(userManagerMock);

        User anonymousUserMock = mock(User.class);
        when(userManagerMock.getAnonymousUser()).thenReturn(anonymousUserMock);
        when(anonymousUserMock.getName()).thenReturn("anonymous");

        mgnlVersionSession = MgnlContext.getJCRSession("mgnlVersion");

        websiteSession = MgnlContext.getJCRSession("website");

        util = CopyUtil.getInstance();
        BootstrapUtil.bootstrap(new String[] { "/mgnlVersion.test2.xml", "/website.test.xml", "/website.homePage.xml" }, ImportUUIDBehavior.IMPORT_UUID_COLLISION_THROW);
        websiteSession.save();
        mgnlVersionSession.save();
        srcTest = websiteSession.getNode("/test");
        srcTest.setProperty("mgnl:ordering_info", "nothing");
        srcTest.setProperty("mgnl:path", "/");
        srcTest.setProperty("mgnl:activation_stamp", System.currentTimeMillis());
        websiteSession.save();

        // add some extra paragraphs (e.g. emulate activation of new paragraph
        srcTest.addNode("paraNew", MgnlNodeType.NT_CONTENTNODE);
        websiteSession.save();

        filter = new AbstractPredicate<Node>() {

            @Override
            public boolean evaluateTyped(Node t) {
                return true;
            }
        };
    }




    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();
        MgnlContext.setInstance(null);
        ComponentsTestUtil.clear();
    }


    @Test
    public void testToVersionReferencable() throws Exception {
        // GIVEN
        Node para = srcTest.addNode("para", MgnlNodeType.NT_CONTENTNODE);
        assertTrue(para.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        websiteSession.save();
        // WHEN
        util.copyToversion(srcTest, filter);
        // THEN ... do not throw any exception
    }

    @Test
    public void testToVersionNonReferencable() throws Exception {
        // GIVEN
        Node nonReferencable = srcTest.addNode("para", MgnlNodeType.NT_UNSTRUCTURED);
        assertFalse(nonReferencable.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        websiteSession.save();
        // WHEN
        util.copyToversion(nonReferencable, filter);
        // THEN ... do not throw any exception
    }

    @Test
    public void testToVersionNonReferencableChild() throws Exception {
        // GIVEN
        Node para = srcTest.addNode("para", MgnlNodeType.NT_UNSTRUCTURED);
        assertFalse(para.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        websiteSession.save();
        // WHEN
        util.copyToversion(srcTest, filter);
        // THEN ... do not throw any exception
    }

    @Test
    public void testFromVersionReferencable() throws Exception {
        // GIVEN
        versioned = mgnlVersionSession.getNode("/test2");
        Node para = versioned.addNode("para", MgnlNodeType.NT_CONTENTNODE);
        assertTrue(para.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        mgnlVersionSession.save();
        // WHEN
        util.copyFromVersion(versioned, srcTest, filter);
        // THEN ... do not throw any exception
    }

    @Test
    public void testFromVersionNonReferencableChild() throws Exception {
        // GIVEN
        versioned = mgnlVersionSession.getNodeByIdentifier("aa672c4f-3a99-49fc-8ad8-48c6b0d70da3");
        Node para = versioned.addNode("para", MgnlNodeType.NT_UNSTRUCTURED);
        assertFalse(para.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        mgnlVersionSession.save();
        // WHEN
        util.copyFromVersion(versioned, srcTest, filter);
        // THEN ... do not throw any exception
    }

    @Test
    public void testFromVersionNonReferencable() throws Exception {
        // GIVEN
        versioned = mgnlVersionSession.getNodeByIdentifier("aa672c4f-3a99-49fc-8ad8-48c6b0d70da3");
        Node nonReferencable = versioned.addNode("para", MgnlNodeType.NT_UNSTRUCTURED);
        assertFalse(nonReferencable.isNodeType(MgnlNodeType.MIX_REFERENCEABLE));
        mgnlVersionSession.save();
        // WHEN
        util.copyFromVersion(nonReferencable, srcTest, filter);
        // THEN ... do not throw any exception
    }
}
