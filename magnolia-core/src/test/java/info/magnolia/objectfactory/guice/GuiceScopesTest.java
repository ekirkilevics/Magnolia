/**
 * This file Copyright (c) 2011-2012 Magnolia International
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
package info.magnolia.objectfactory.guice;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.SystemProperty;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.WebContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.objectfactory.annotation.LocalScoped;
import info.magnolia.objectfactory.annotation.SessionScoped;
import info.magnolia.objectfactory.configuration.ComponentProviderConfiguration;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.mock.MockWebContext;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.After;
import org.junit.Test;

import com.google.inject.ProvisionException;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockHttpSession;

public class GuiceScopesTest {

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        SystemProperty.clear();
        MgnlContext.setInstance(null);
        Components.setComponentProvider(null);
    }

    @Singleton
    public static class MockSingletonWithContextProviders {
        @Inject
        Provider<Context> contextProvider;
        @Inject
        Provider<WebContext> webContextProvider;
        @Inject
        Provider<AggregationState> aggregationStateProvider;
        @Inject
        Provider<HttpServletRequest> requestProvider;
        @Inject
        Provider<HttpServletResponse> responseProvider;
        @Inject
        Provider<HttpSession> sessionProvider;
    }

    @LocalScoped
    public static class MockLocalScopedObject {
    }

    @SessionScoped
    public static class MockSessionScopedObject {
    }

    @Test
    public void testContextProvidersWhenContextSet() {
        // GIVEN
        Context context = mock(Context.class);
        MgnlContext.setInstance(context);
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSingletonWithContextProviders.class);

        // WHEN
        MockSingletonWithContextProviders component = provider.getComponent(MockSingletonWithContextProviders.class);

        // THEN
        assertSame(context, component.contextProvider.get());
        // HAD: web context provider is called also on preDestroy
        assertNull(component.webContextProvider.get());
        assertNull(component.aggregationStateProvider.get());
    }

    // TODO its unfortunate that these throw ProvisionException
    @Test
    public void testContextProvidersWhenWebContextSet() {
        // GIVEN
        AggregationState aggregationState = new AggregationState();
        WebContext context = mock(WebContext.class);
        when(context.getRequest()).thenReturn(new MockHttpServletRequest());
        when(context.getAggregationState()).thenReturn(aggregationState);
        MgnlContext.setInstance(context);
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSingletonWithContextProviders.class);

        // WHEN
        MockSingletonWithContextProviders component = provider.getComponent(MockSingletonWithContextProviders.class);

        // THEN
        assertSame(context, component.contextProvider.get());
        assertSame(context, component.webContextProvider.get());
        assertSame(aggregationState, component.aggregationStateProvider.get());
    }
    @Test
    public void testServletProvidersWhenWebContextSet() {
        // GIVEN
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpSession session = new MockHttpSession();
        request.setSession(session);
        MockWebContext webContext = new MockWebContext();
        webContext.setRequest(request);
        webContext.setResponse(response);
        MgnlContext.setInstance(webContext);
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSingletonWithContextProviders.class);

        // WHEN
        MockSingletonWithContextProviders component = provider.getComponent(MockSingletonWithContextProviders.class);

        // THEN
        assertSame(request, component.requestProvider.get());
        assertSame(session, component.sessionProvider.get());
        assertSame(response, component.responseProvider.get());
    }
    @Test
    public void testServletProvidersFailWhenWebContextNotSet() {
        // GIVEN
        MgnlContext.setInstance(mock(Context.class));
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSingletonWithContextProviders.class);

        // WHEN
        MockSingletonWithContextProviders component = provider.getComponent(MockSingletonWithContextProviders.class);

        // THEN
        assertNull(component.requestProvider.get());
        try {
            component.sessionProvider.get();
            fail();
        } catch (ProvisionException expected) {
        }
        assertNull(component.responseProvider.get());
    }
    @Test
    public void testRequestScope() {
        // GIVEN
        MockWebContext webContext = new MockWebContext();
        webContext.setRequest(new MockHttpServletRequest());
        webContext.setResponse(new MockHttpServletResponse());
        MgnlContext.setInstance(webContext);
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockLocalScopedObject.class);

        // WHEN
        MockLocalScopedObject component = provider.getComponent(MockLocalScopedObject.class);

        // THEN
        assertNotNull(component);
        assertSame(component, provider.getComponent(MockLocalScopedObject.class));

        // WHEN we switch request
        webContext = new MockWebContext();
        webContext.setRequest(new MockHttpServletRequest());
        webContext.setResponse(new MockHttpServletResponse());
        MgnlContext.setInstance(webContext);

        // THEN we get a new object
        MockLocalScopedObject component2 = provider.getComponent(MockLocalScopedObject.class);
        assertNotNull(component2);
        assertNotSame(component2, component);
    }

    @Test
    public void testRequestScopeFailsWhenNotInWebContext() {
        // GIVEN
        MgnlContext.setInstance(mock(Context.class));
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockLocalScopedObject.class);

        // WHEN - THEN
        assertNull(provider.getComponent(MockLocalScopedObject.class));
    }
    @Test
    public void testSessionScope() {
        // GIVEN
        MockWebContext webContext = new MockWebContext();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setSession(new MockHttpSession());
        webContext.setRequest(request);
        MgnlContext.setInstance(webContext);

        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSessionScopedObject.class);

        // WHEN
        MockSessionScopedObject component = provider.getComponent(MockSessionScopedObject.class);

        // THEN
        assertNotNull(component);
        assertSame(component, provider.getComponent(MockSessionScopedObject.class));

        // WHEN we switch request
        webContext = new MockWebContext();
        request = new MockHttpServletRequest();
        webContext.setRequest(request);
        request.setSession(new MockHttpSession());
        MgnlContext.setInstance(webContext);

        // THEN we get a new object
        MockSessionScopedObject component2 = provider.getComponent(MockSessionScopedObject.class);
        assertNotNull(component2);
        assertNotSame(component2, component);
    }

    @Test(expected = ProvisionException.class)
    public void testSessionScopeFailsWhenNotInWebContext() {
        // GIVEN
        MgnlContext.setInstance(mock(Context.class));
        GuiceComponentProvider provider = createComponentProviderWithSingleImplementation(MockSessionScopedObject.class);

        // WHEN
        provider.getComponent(MockSessionScopedObject.class);

        // THEN we expect an exception
    }

    private GuiceComponentProvider createComponentProviderWithSingleImplementation(Class<?> clazz) {
        ComponentProviderConfiguration configuration = new ComponentProviderConfiguration();
        configuration.registerImplementation(clazz);
        return new GuiceComponentProviderBuilder().withConfiguration(configuration).build();
    }
}
