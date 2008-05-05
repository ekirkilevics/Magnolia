/**
 * This file Copyright (c) 2008 Magnolia International
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
package info.magnolia.module.cache.filter;

import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.voting.DefaultVoting;
import info.magnolia.voting.Voting;
import info.magnolia.voting.voters.TrueVoter;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import static info.magnolia.test.TestUtil.enumeration;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class StandaloneGZipFilterTest extends TestCase {
    private HttpServletRequest request;

    public void testBypassesAsDictatedByVoters() {
        doTest(true, true);
    }

    public void testBypassesIfClientDoesNotAcceptGZip() {
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration());
        doTest(false, true);
    }

    public void testDoesNotByPassIfClientAcceptsGZip() {
        expect(request.getHeaders("Accept-Encoding")).andReturn(enumeration("foo", "gzip", "bar"));
        doTest(false, false);
    }

    private void doTest(boolean voterReturns, boolean expectedBypass) {
        final TrueVoter voter = new TrueVoter();
        voter.setNot(!voterReturns);
        final Voting voting = new DefaultVoting();
        FactoryUtil.setInstance(Voting.class, voting);
        final StandaloneGZipFilter filter = new StandaloneGZipFilter();
        filter.addBypass(voter);

        replay(request);
        assertEquals(expectedBypass, filter.bypasses(request));
        verify(request);
    }

    protected void setUp() throws Exception {
        super.setUp();
        request = createStrictMock(HttpServletRequest.class);
    }


}
