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
package info.magnolia.setup.for4_5;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.module.InstallContextImpl;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.Version;
import info.magnolia.test.RepositoryTestCase;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static info.magnolia.test.hamcrest.ContentMatchers.hasContent;
import static info.magnolia.test.hamcrest.ContentMatchers.hasNodeData;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.*;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class UpdateSecurityFilterClientCallbacksConfigurationTest extends RepositoryTestCase {
    private final String OLD_FILTER_PATH = "/server/filters/dummySecurityFilter";
    private final String NEW_FILTER_PATH = "/server/filters/securityCallback";
    private InstallContextImpl ctx;
    private ModuleRegistry moduleRegistry;

    @Before
    public void before() throws Exception {
        moduleRegistry = mock(ModuleRegistry.class);
        ctx = spy(new InstallContextImpl(moduleRegistry));
        ctx.setCurrentModule(new ModuleDefinition("test-module", Version.parseVersion("0"), null, null));
    }

    @After
    public void after() throws Exception {
        verifyZeroInteractions(moduleRegistry);
//        ComponentsTestUtil.clear();
//        SystemProperty.clear();
//        MgnlContext.setInstance(null);
//        Components.setComponentProvider(null);
    }

    private static final String[] NEW_FILTER_PRE_CREATED = new String[]{
            "/server/filters/securityCallback@type=mgnl:content",
            "/server/filters/securityCallback.class=info.magnolia.cms.security.SecurityCallbackFilter",
            "/server/filters/securityCallback/bypasses@type=mgnl:contentNode",
            "/server/filters/securityCallback/bypasses.dummy=prout",
    };

    private static final String[] STANDARD_DEFAULT_CONFIG = new String[]{
            "/server/filters/dummySecurityFilter@type=mgnl:content",
            "/server/filters/dummySecurityFilter.class=info.magnolia.but.seriously.this.filter.class.should.not.be.needed.for.the.test",
            "/server/filters/dummySecurityFilter/clientCallback@type=mgnl:contentNode",
            "/server/filters/dummySecurityFilter/clientCallback.class=info.magnolia.cms.security.auth.callback.FormClientCallback",
            "/server/filters/dummySecurityFilter/clientCallback.loginForm=/mgnl-resources/loginForm/login.html",
            "/server/filters/dummySecurityFilter/clientCallback.realmName=Magnolia"
    };

    @Test
    public void rearrangementOfBasicDefaultConfig() throws Exception {
        final HierarchyManager hm = setupHM(STANDARD_DEFAULT_CONFIG);
        when(ctx.getConfigHierarchyManager()).thenReturn(hm);

        final UpdateSecurityFilterClientCallbacksConfiguration task = new UpdateSecurityFilterClientCallbacksConfiguration("dummySecurityFilter", "securityCallback");
        task.execute(ctx);

        final Content oldFilterNode = hm.getContent(OLD_FILTER_PATH);
        assertThat(oldFilterNode, allOf(
                not(hasContent("clientCallback")),
                not(hasContent("clientCallbacks"))
        ));
        final Content newFilterNode = hm.getContent(NEW_FILTER_PATH);
        assertThat(newFilterNode, allOf(
                not(hasContent("clientCallback")),
                hasContent("clientCallbacks", "mgnl:contentNode")
        ));

        final Content callbacks = newFilterNode.getContent("clientCallbacks");
        assertThat(callbacks.getChildren(), hasSize(1));
        assertThat(callbacks.getNodeDataCollection(), Matchers.<NodeData>empty());
        assertThat(callbacks, hasContent("form", "mgnl:contentNode"));

        final Content formCallback = callbacks.getContent("form");
        // TODO empty() matcher doesn't play well with generics -  see http://code.google.com/p/hamcrest/issues/detail?id=97
        assertThat(formCallback.getChildren(), Matchers.<Content>empty());

        // TODO - no Hamcrest matcher could combine those, but we should be able to write something that combines it.
        // hasProperties() or hasOnlyTheseProperties(name/value pairs) - see org.hamcrest.beans.SamePropertyValuesAs.PropertyMatcher
        assertThat(formCallback.getNodeDataCollection(), hasSize(2));
        assertThat(formCallback, allOf(
                hasNodeData("class", "info.magnolia.cms.security.auth.callback.FormClientCallback"),
                hasNodeData("loginForm", "/mgnl-resources/loginForm/login.html"),
                // also check that we did not copy the unnecessary realmName property
                not(hasNodeData("realmName", "Magnolia"))
        ));

        assertThat(ctx.getMessages().size(), equalTo(1));
        assertThat(ctx.getMessages(), allOf(
                hasEntry(equalTo("test-module (version 0.0.0)"), allOf(
                        hasSize(1),
                        contains(
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("/server/filters/dummySecurityFilter/clientCallback has a 'realmName' property; it is ignored and has been removed."))
                                )
                        )
                ))
        ));
    }

    private static final String[] STANDARD_DELEGATING_CONFIG = new String[]{
            "/server/filters/dummySecurityFilter@type=mgnl:content",
            "/server/filters/dummySecurityFilter.class=info.magnolia.but.seriously.this.filter.class.should.not.be.needed.for.the.test",
            "/server/filters/dummySecurityFilter/clientCallback@type=mgnl:contentNode",
            "/server/filters/dummySecurityFilter/clientCallback.class=info.magnolia.cms.security.auth.callback.CompositeCallback",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/public.class=info.magnolia.cms.util.UrlPatternDelegate",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/public.url=/demo-project/members-area/protected*",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/public/delegate.class=info.magnolia.cms.security.auth.callback.RedirectClientCallback",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/public/delegate.location=/demo-project/members-area/login.html",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/public/delegate.realmName=Magnolia",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia.class=info.magnolia.cms.util.UrlPatternDelegate",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia.url=*",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia/delegate.class=info.magnolia.cms.security.auth.callback.FormClientCallback",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia/delegate.loginForm=/mgnl-resources/loginForm/login.html",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia/delegate.realmName=Magnolia"
    };

    @Test
    public void rearrangementOfSTKsDefaultConfig() throws Exception {
        final HierarchyManager hm = setupHM(STANDARD_DELEGATING_CONFIG);
        when(ctx.getConfigHierarchyManager()).thenReturn(hm);

        final UpdateSecurityFilterClientCallbacksConfiguration task = new UpdateSecurityFilterClientCallbacksConfiguration("dummySecurityFilter", "securityCallback");
        task.execute(ctx);

        final Content oldFilterNode = hm.getContent(OLD_FILTER_PATH);
        assertThat(oldFilterNode, allOf(
                not(hasContent("clientCallback")),
                not(hasContent("clientCallbacks"))
        ));
        final Content newFilterNode = hm.getContent(NEW_FILTER_PATH);
        assertThat(newFilterNode, allOf(
                not(hasContent("clientCallback")),
                hasContent("clientCallbacks", "mgnl:contentNode")
        ));

        final Content callbacks = newFilterNode.getContent("clientCallbacks");
        assertThat(callbacks.getChildren(), hasSize(2));
        assertThat(callbacks.getNodeDataCollection(), Matchers.<NodeData>empty());
        assertThat(callbacks, allOf(
                hasContent("public", "mgnl:contentNode"),
                hasContent("magnolia", "mgnl:contentNode"),
                not(hasContent("form"))
        ));

        final Content publicCallback = callbacks.getContent("public");
        final Content magnoliaCallback = callbacks.getContent("magnolia");
        assertThat(publicCallback.getChildren(), hasSize(1));
        assertThat(magnoliaCallback.getChildren(), hasSize(1));

        assertThat(publicCallback.getNodeDataCollection(), hasSize(2));
        assertThat(publicCallback, allOf(
                hasNodeData("class", "info.magnolia.cms.security.auth.callback.RedirectClientCallback"),
                hasNodeData("location", "/demo-project/members-area/login.html")
        ));
        assertThat(magnoliaCallback.getNodeDataCollection(), hasSize(2));
        assertThat(magnoliaCallback, allOf(
                hasNodeData("class", "info.magnolia.cms.security.auth.callback.FormClientCallback"),
                hasNodeData("loginForm", "/mgnl-resources/loginForm/login.html")
        ));


        assertThat(ctx.getMessages().size(), equalTo(1));
        assertThat(ctx.getMessages(), allOf(
                hasEntry(equalTo("test-module (version 0.0.0)"), allOf(
                        hasSize(2),
                        contains(
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("/server/filters/dummySecurityFilter/clientCallback/patterns/public/delegate has a 'realmName' property; it is ignored and has been removed."))
                                ),
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("/server/filters/dummySecurityFilter/clientCallback/patterns/magnolia/delegate has a 'realmName' property; it is ignored and has been removed."))
                                )
                        )
                ))
        ));
    }

    private static final String[] NON_STANDARD_CONFIG = new String[]{
            "/server/filters/dummySecurityFilter@type=mgnl:content",
            "/server/filters/dummySecurityFilter.class=info.magnolia.but.seriously.this.filter.class.should.not.be.needed.for.the.test",
            "/server/filters/dummySecurityFilter/clientCallback@type=mgnl:contentNode",
            "/server/filters/dummySecurityFilter/clientCallback/class=org.acme.CustomClientCallbackClass",
            "/server/filters/dummySecurityFilter/clientCallback/loginForm=/mgnl-resources/loginForm/login.html",
            "/server/filters/dummySecurityFilter/clientCallback/someCustomProperty=arbitrary value"
    };

    @Test
    public void simpleCustomCallbackMovedAndBackedUpWithWarning() throws Exception {
        final HierarchyManager hm = setupHM(NON_STANDARD_CONFIG);
        when(ctx.getConfigHierarchyManager()).thenReturn(hm);

        final UpdateSecurityFilterClientCallbacksConfiguration task = new UpdateSecurityFilterClientCallbacksConfiguration("dummySecurityFilter", "securityCallback");
        task.execute(ctx);

        final Content oldFilterNode = hm.getContent(OLD_FILTER_PATH);
        assertThat(oldFilterNode, allOf(
                not(hasContent("clientCallback")),
                not(hasContent("clientCallbacks")),
                hasContent("_clientCallback_backup_config")
        ));
        final Content newFilterNode = hm.getContent(NEW_FILTER_PATH);
        assertThat(newFilterNode, allOf(
                not(hasContent("clientCallback")),
                hasContent("clientCallbacks", "mgnl:contentNode")
        ));

        final Content backup = oldFilterNode.getContent("_clientCallback_backup_config");
        assertThat(backup.getChildren(), hasSize(0));
        assertThat(backup.getNodeDataCollection(), hasSize(3));
        assertThat(backup, allOf(
                hasNodeData("class", "org.acme.CustomClientCallbackClass"),
                hasNodeData("loginForm", "/mgnl-resources/loginForm/login.html"),
                hasNodeData("someCustomProperty", "arbitrary value")
        ));

        final Content callbacks = newFilterNode.getContent("clientCallbacks");
        assertThat(callbacks.getChildren(), hasSize(1));
        assertThat(callbacks.getNodeDataCollection(), Matchers.<NodeData>empty());
        assertThat(callbacks, hasContent("custom", "mgnl:contentNode"));

        final Content customCallback = callbacks.getContent("custom");
        assertThat(customCallback.getChildren(), Matchers.<Content>empty());
        assertThat(customCallback.getNodeDataCollection(), hasSize(3));
        assertThat(customCallback, allOf(
                hasNodeData("class", "org.acme.CustomClientCallbackClass"),
                hasNodeData("loginForm", "/mgnl-resources/loginForm/login.html"),
                hasNodeData("someCustomProperty", "arbitrary value")
        ));

        assertThat(ctx.getMessages().size(), equalTo(1));
        assertThat(ctx.getMessages(), allOf(
                hasEntry(equalTo("test-module (version 0.0.0)"), allOf(
                        hasSize(1),
                        contains(
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("Client callback configuration for dummySecurityFilter was not standard: an untouched copy of /server/filters/dummySecurityFilter/clientCallback has been kept at /server/filters/dummySecurityFilter/_clientCallback_backup_config. Please check, validate and correct the new configuration at /server/filters/securityCallback/clientCallbacks."))
                                )
                        )
                ))
        ));
    }

    private static final String[] NON_STANDARD_DELEGATING_CONFIG = new String[]{
            "/server/filters/dummySecurityFilter@type=mgnl:content",
            "/server/filters/dummySecurityFilter.class=info.magnolia.but.seriously.this.filter.class.should.not.be.needed.for.the.test",
            "/server/filters/dummySecurityFilter/clientCallback@type=mgnl:contentNode",
            "/server/filters/dummySecurityFilter/clientCallback.class=info.magnolia.cms.security.auth.callback.CompositeCallback",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom1.class=org.foobar.CustomDelegate",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom1.url=/path1/*",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom1/delegate.class=org.foobar.CustomCallback1",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom1/delegate.location=/demo-project/members-area/login.html",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom1/delegate.lala=lolo",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom2.class=info.magnolia.cms.util.UrlPatternDelegate",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom2.url=/path2/*",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom2/delegate.class=org.foobar.CustomCallback2",
            "/server/filters/dummySecurityFilter/clientCallback/patterns/custom2/delegate.dings=bums"
    };

    @Test
    public void nonStandardCompositeConfigIsBackedUp() throws Exception {
        final HierarchyManager hm = setupHM(NON_STANDARD_DELEGATING_CONFIG);
        when(ctx.getConfigHierarchyManager()).thenReturn(hm);

        final UpdateSecurityFilterClientCallbacksConfiguration task = new UpdateSecurityFilterClientCallbacksConfiguration("dummySecurityFilter", "securityCallback");
        task.execute(ctx);

        final Content oldFilterNode = hm.getContent(OLD_FILTER_PATH);
        assertThat(oldFilterNode, allOf(
                not(hasContent("clientCallback")),
                not(hasContent("clientCallbacks")),
                hasContent("_clientCallback_backup_config")
        ));
        final Content newFilterNode = hm.getContent(NEW_FILTER_PATH);
        assertThat(newFilterNode, allOf(
                not(hasContent("clientCallback")),
                hasContent("clientCallbacks", "mgnl:contentNode")
        ));

        final Content backup = oldFilterNode.getContent("_clientCallback_backup_config");
        assertThat(backup.getChildren(), hasSize(1));
        assertThat(backup.getNodeDataCollection(), hasSize(1));
        assertThat(backup, allOf(
                hasNodeData("class", "info.magnolia.cms.security.auth.callback.CompositeCallback"),
                hasContent("patterns", "mgnl:contentNode"),
                hasContent("patterns/custom1", "mgnl:contentNode")
                // too lazy to assert the whole thing was backed up...
        ));

        final Content callbacks = newFilterNode.getContent("clientCallbacks");
        assertThat(callbacks.getChildren(), hasSize(1));
        assertThat(callbacks.getNodeDataCollection(), Matchers.<NodeData>empty());
        assertThat("we can't migrate custom1, it doesn't have a known delegator class", callbacks, not(hasContent("custom1", "mgnl:contentNode")));
        assertThat(callbacks, hasContent("custom2", "mgnl:contentNode"));

        final Content customCallback2 = callbacks.getContent("custom2");
        assertThat(customCallback2.getNodeDataCollection(), hasSize(2));
        assertThat(customCallback2, allOf(
                hasNodeData("class", "org.foobar.CustomCallback2"),
                hasNodeData("dings", "bums")
        ));

        assertThat(ctx.getMessages().size(), equalTo(1));
        assertThat(ctx.getMessages(), allOf(
                hasEntry(equalTo("test-module (version 0.0.0)"),
                        // no need to check for size, contains() ensures each matcher matches, in order
                        contains(
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("Unknown callback class at /server/filters/dummySecurityFilter/clientCallback/patterns/custom1:org.foobar.CustomDelegate"))
                                ),
                                allOf(
                                        notNullValue(),
                                        hasProperty("priority", is(InstallContext.MessagePriority.warning)),
                                        hasProperty("message", is("Client callback configuration for dummySecurityFilter was not standard: an untouched copy of /server/filters/dummySecurityFilter/clientCallback has been kept at /server/filters/dummySecurityFilter/_clientCallback_backup_config. Please check, validate and correct the new configuration at /server/filters/securityCallback/clientCallbacks."))
                                )
                        )
                )
        ));
    }

    private HierarchyManager setupHM(String... lines) throws IOException, RepositoryException {
        // TODO can't use MockHierarchyManager, as it doesn't support the move method used by this task - which is why we're currently extending RepositoryTestCase ...
        // return MockUtil.createAndSetHierarchyManager("config", StringUtils.join(lines, '\n'));
        final HierarchyManager hm = MgnlContext.getHierarchyManager("config");
        final String newFilterPreCreated = StringUtils.join(NEW_FILTER_PRE_CREATED, '\n');
        final String testData = StringUtils.join(lines, '\n');
        final ByteArrayInputStream props = new ByteArrayInputStream((newFilterPreCreated + testData).getBytes());
        new PropertiesImportExport().createContent(hm.getRoot(), props);
        return hm;
    }

}
