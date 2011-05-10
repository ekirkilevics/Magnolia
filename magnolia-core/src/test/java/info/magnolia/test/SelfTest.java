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
package info.magnolia.test;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.test.mock.MockContext;
import junit.framework.TestCase;
import org.apache.jackrabbit.core.jndi.RegistryHelper;
import org.apache.jackrabbit.core.jndi.provider.DummyInitialContextFactory;

import javax.jcr.Repository;
import javax.jcr.SimpleCredentials;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Hashtable;
import java.util.Locale;

/**
 * Ensures some issues we encountered with 3rd party libraries are gone for good.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class SelfTest extends TestCase {

    @Override
    protected void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        MgnlContext.setInstance(null);
        super.tearDown();
    }

    /**
     * Jackrabbit keeps a cache of jndi references since 1.4.6
     * See https://issues.apache.org/jira/browse/JCR-1778
     */
    public void testJackrabbitUnregistersProperly() throws Exception {
        Hashtable environment = new Hashtable();
        environment.put(Context.INITIAL_CONTEXT_FACTORY, DummyInitialContextFactory.class.getName());
        environment.put(Context.PROVIDER_URL, "http://jackrabbit.apache.org/");
        Context context = new InitialContext(environment);

        String xml = "src/test/resources/repo-conf/jackrabbit-memory-search.xml";
        String dir = "target/repository";
        String key = "repository";

        // Create first repository
        {
            RegistryHelper.registerRepository(context, key, xml, dir, true);
            final Repository repository = (Repository) context.lookup(key);
            repository.login(new SimpleCredentials("admin", "admin".toCharArray())).logout(); // throws an IllegalStateException!
            RegistryHelper.unregisterRepository(context, key);
        }

        RepositoryTestCase.workaroundJCR1778();
        // Create second repository with the same configuration
        {
            RegistryHelper.registerRepository(context, key, xml, dir, true);
            final Repository repository = (Repository) context.lookup(key);
            repository.login().logout(); // throws an IllegalStateException!
            RegistryHelper.unregisterRepository(context, key);
        }
    }

    /**
     * This test breaks currently when run against commons-beanutils 1.8, but works fine with version 1.7
     */
    public void testCommandIsSetCorrectlyFromPrototype() throws Exception {
        MockContext ctx = new MockContext();
        ctx.setLocale(Locale.ENGLISH);
        ComponentsTestUtil.setImplementation(SystemContext.class, MockContext.class);
        ComponentsTestUtil.setImplementation(MessagesManager.class, DefaultMessagesManager.class);
        MgnlContext.setInstance(ctx);
        new TestCommand().execute((org.apache.commons.chain.Context) ctx);
    }

}
