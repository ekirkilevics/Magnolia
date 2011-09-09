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
package info.magnolia.context;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.version.MgnlVersioningSession;
import info.magnolia.cms.util.JCRPropertyFilteringSessionWrapper;
import info.magnolia.test.RepositoryTestCase;

import javax.jcr.Session;

import org.junit.Test;

/**
 * @version $Id$
 */
public class AbstractRepositoryStrategyTest extends RepositoryTestCase {
    @Test
    public void testUnwrap() {
        // GIVEN
        final UserContext context = mock(UserContext.class);
        final Session jcrSession = mock(Session.class);
        final Session wrapperOne = new JCRPropertyFilteringSessionWrapper(jcrSession);
        final Session wrapperTwo = new MgnlVersioningSession(wrapperOne);

        DefaultRepositoryStrategy strategy = new DefaultRepositoryStrategy(context);

        // WHEN
        Session result = strategy.unwrap(wrapperOne);

        // THEN
        assertEquals(jcrSession, result);
    }

    @Test
    public void testCreatedHMCanAlsoBeRetrievedViaJCRSession() {
        // GIVEN
        HierarchyManager hm = MgnlContext.getInstance().getHierarchyManager("config");
        Session session = hm.getWorkspace().getSession();

        // WHEN
        HierarchyManager hm2 =  ((AbstractContext) MgnlContext.getInstance()).getRepositoryStrategy().getHierarchyManagerFor(session);

        // THEN
        assertEquals(hm, hm2);
    }
}
