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
package info.magnolia.module.cache.cachepolicy;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.module.cache.CachePolicy;
import junit.framework.TestCase;

/**
 * Test Default cache policy
 * @author ochytil
 * @version $Id:$
 */
public class DefaultTest extends TestCase{

    private CachePolicy policy;
    private AggregationState aggregationState;
    private Locale locale;
    private WebContext webCtx;
    private HttpServletRequest request;
    
    @Override
    public void setUp(){
        webCtx = createMock(WebContext.class);
        request = createMock(HttpServletRequest.class);
        aggregationState = new AggregationState();
    }
    
    public void testRetrieveDefaultCacheKey(){
        policy = new Default();
        String serverName = "test";
        Map<String, String> params = new HashMap<String, String>();
        params.put("testkey", "testvalue");
        Boolean isSecure = false;
        String uri = "localhost";
        
        MgnlContext.setInstance(webCtx);

        expect(MgnlContext.getContextPath()).andReturn(uri);
        expect(MgnlContext.getWebContext().getRequest()).andReturn(request);
        expect(request.getServerName()).andReturn(serverName);
        expect(MgnlContext.getWebContext().getParameters()).andReturn(params);
        expect(MgnlContext.getWebContext().getRequest()).andReturn(request);
        expect(request.isSecure()).andReturn(isSecure);

        Object[] mocks = new Object[] { request, webCtx };
        replay(mocks);
        aggregationState.setCharacterEncoding("UTF-8");
        aggregationState.setLocale(locale.ENGLISH);
        aggregationState.setOriginalURI(uri);

        assertEquals(policy.retrieveCacheKey(aggregationState).toString(), "DefaultCacheKey{uri='localhost', serverName='test', locale='en', params={testkey=testvalue}', secure='false'}");

        verify(mocks);
    }
    
    @Override
    protected void tearDown() throws Exception {
        MgnlContext.setInstance(null);
        super.tearDown();
    }
}
