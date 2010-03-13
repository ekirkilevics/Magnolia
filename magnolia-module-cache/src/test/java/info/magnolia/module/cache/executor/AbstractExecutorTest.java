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
package info.magnolia.module.cache.executor;

import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.CachePolicyResult;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class AbstractExecutorTest extends TestCase {
    private final static long ONE_SECOND_IN_MSECS = 1000L;

    private HttpServletRequest request;

    protected void setUp() throws Exception {
        //MockUtil.initMockContext();
        request = createStrictMock(HttpServletRequest.class);
    }

    protected void tearDown() throws Exception {
        verify(request);
    }

    public void testNotModifiedAfterRequestHeader() throws Exception {

        final long modifiedTime = System.currentTimeMillis();
        long headerModifiedSince = modifiedTime + (ONE_SECOND_IN_MSECS * 2); // Request time is 2 seconds after modified time
        expect(request.getDateHeader("If-Modified-Since")).andReturn(headerModifiedSince);
        expect(request.getHeader("If-None-Match")).andReturn(null);
        replay(request);

        AbstractExecutor executor = new TestExecutor();
        boolean modifiedSince = executor.ifModifiedSince(request, modifiedTime);

        assertFalse("Request if-modified-since is 2 seconds after modification time - should have returned false", modifiedSince);
    }

    public void testModifiedAfterRequestHeader() throws Exception {

        final long modifiedTime = System.currentTimeMillis();
        long headerModifiedSince = modifiedTime - (ONE_SECOND_IN_MSECS * 2); // Request time is 2 seconds before modified time
        expect(request.getDateHeader("If-Modified-Since")).andReturn(headerModifiedSince);
        expect(request.getHeader("If-None-Match")).andReturn(null);
        replay(request);

        AbstractExecutor executor = new TestExecutor();
        boolean modifiedSince = executor.ifModifiedSince(request, modifiedTime);

        assertTrue("Request if-modified-since is 2 seconds before modification time - should have returned true", modifiedSince);
    }

    public void testModifiedSameAsRequestHeader() throws Exception {

        final long modifiedTime = System.currentTimeMillis();
        long headerModifiedSince = modifiedTime; // Request time is same as modified time
        expect(request.getDateHeader("If-Modified-Since")).andReturn(headerModifiedSince);
        expect(request.getHeader("If-None-Match")).andReturn(null);
        replay(request);

        AbstractExecutor executor = new TestExecutor();
        boolean modifiedSince = executor.ifModifiedSince(request, modifiedTime);

        assertFalse("Request if-modified-since is the same as modification time - should have returned false", modifiedSince);
    }

    /*
    No-op implementation purely here so we can test abstract methods
     */
    class TestExecutor extends AbstractExecutor {

        public void processCacheRequest(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Cache cache, CachePolicyResult cachePolicyResult) throws IOException, ServletException {
            // do nothing
        }
    }
}
