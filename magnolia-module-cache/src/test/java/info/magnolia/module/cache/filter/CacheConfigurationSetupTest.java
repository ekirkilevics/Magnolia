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
package info.magnolia.module.cache.filter;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.cms.security.User;
import info.magnolia.cms.security.UserManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.module.ModuleManagementException;
import info.magnolia.module.cache.CacheConfiguration;
import info.magnolia.module.cache.CachePolicyExecutor;
import info.magnolia.module.cache.CachePolicyResult;
import info.magnolia.module.cache.cachepolicy.Default;
import info.magnolia.module.cache.executor.CompositeExecutor;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.reader.BetwixtModuleDefinitionReader;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.RepositoryTestCase;
import info.magnolia.test.mock.MockUtil;
import info.magnolia.test.mock.MockWebContext;
import info.magnolia.voting.voters.VoterSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;

/**
 * Test for common cache configuration setup.
 * @version $Id$
 */
public class CacheConfigurationSetupTest extends RepositoryTestCase {

    private CacheConfiguration cacheConf;

    @Override
    protected List<ModuleDefinition> getModuleDefinitionsForTests() throws ModuleManagementException {
        final ModuleDefinition core = new BetwixtModuleDefinitionReader().readFromResource("/META-INF/magnolia/core.xml");
        final ModuleDefinition cache = new BetwixtModuleDefinitionReader().readFromResource("/META-INF/magnolia/cache.xml");
        return Arrays.asList(core, cache);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bootstrapSingleResource("/mgnl-bootstrap/cache/config.modules.cache.config.configurations.default.xml");
        Content content = ContentUtil.getContent("config", "/modules/cache/config/configurations/default");

        User anonymous = createMock(User.class);
        expect(anonymous.getName()).andStubReturn(UserManager.ANONYMOUS_USER);
        replay(anonymous);

        MgnlContext.login(MockUtil.createSubject(anonymous));

        Node2BeanProcessor n2b = Components.getComponent(Node2BeanProcessor.class);
        cacheConf = (CacheConfiguration) n2b.toBean(content.getJCRNode(), CacheConfiguration.class);
    }

    @Test
    public void testVoting(){
        //Logger.getLogger("info.magnolia.voting").setLevel(Level.DEBUG);

        // test public
        assertTrue("normal page should pass", vote("/somepage.html", false, false) > 0);
        assertFalse("normal page with parameters should not pass", vote("/somepage.html", true, false) > 0);
        assertTrue(".resouces should pass", vote("/.resources/somthing.js", false, false) > 0);
        assertFalse(".resouces with parameters should not pass", vote("/.resources/somthing.js", true, false) > 0);
        assertFalse("adminCentral should not pass", vote("/.magnolia/pages/adminCentral.html", false, false) > 0 );
        SystemProperty.setProperty("magnolia.develop", "true");
        assertFalse("javascript.js should not pass if magnolia develop is true", vote("/.magnolia/pages/javascript.js", false, false) > 0);
        SystemProperty.setProperty("magnolia.develop", "false");
        assertTrue("javascript.js should pass if magnolia develop is false", vote("/.magnolia/pages/javascript.js", false, false) > 0);

        // test author
        assertFalse("normal page should not pass on author", vote("/somepage.html", false, true) > 0);
        assertTrue(".resources should pass", vote("/.resources/somthing.js", false, true) > 0);
        assertFalse(".resources with parameters should not pass", vote("/.resources/somthing.js", true, true) > 0);
        assertFalse("adminCentral should not pass", vote("/.magnolia/pages/adminCentral.html", false, true) > 0 );
        SystemProperty.setProperty("magnolia.develop", "true");
        assertFalse("javascript.js should not pass if magnolia develop is true", vote("/.magnolia/pages/javascript.js", false, true) > 0);
        SystemProperty.setProperty("magnolia.develop", "false");
        assertTrue("javascript.js should pass if magnolia develop is false", vote("/.magnolia/pages/javascript.js", false, true) > 0);
    }

    @Test
    public void testExecutorSetup(){
        // this test is mainly testing if the content2bean transformation does what we expect
        CachePolicyExecutor executor = cacheConf.getExecutor(CachePolicyResult.bypass);
        assertTrue(executor instanceof CompositeExecutor);
        executor = cacheConf.getExecutor(CachePolicyResult.useCache);
        assertTrue(executor instanceof CompositeExecutor);
        assertEquals(((CompositeExecutor)executor).getExecutors().length, 2);
    }

    private int vote(String uri, boolean withParameters, boolean admin) {
        setupRequest(uri, withParameters, admin);
        VoterSet voter = ((Default)cacheConf.getCachePolicy()).getVoters();
        return voter.vote(uri);
    }

    private void setupRequest(String uri, boolean parameters, boolean admin) {
        MockWebContext ctx = ((MockWebContext)MgnlContext.getInstance());
        final ServerConfiguration serverConfiguration = new ServerConfiguration();
        serverConfiguration.setAdmin(admin);
        ComponentsTestUtil.setInstance(ServerConfiguration.class, serverConfiguration);
        ctx.setCurrentURI(uri);
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        if(!parameters){
            expect(request.getMethod()).andReturn("GET");
            expect(request.getParameterMap()).andReturn(Collections.EMPTY_MAP);
        }
        else{
            Map parameterMap = new HashMap();
            parameterMap.put("kez", "value");
            expect(request.getMethod()).andReturn("POST");
            expect(request.getParameterMap()).andReturn(parameterMap);
            ctx.setParameters(parameterMap);
        }
        replay(request);
        ctx.setRequest(request);

    }

}
