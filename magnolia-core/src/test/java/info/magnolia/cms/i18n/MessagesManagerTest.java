/**
 * This file Copyright (c) 2003-2009 Magnolia International
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
package info.magnolia.cms.i18n;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.cms.util.FactoryUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;
import static org.easymock.classextension.EasyMock.createMock;

import java.util.Locale;

/**
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MessagesManagerTest extends TestCase {
    private SystemContext sysCtx;
    private Context ctx;
    private static final String DUMMY_BUNDLE = "info.magnolia.cms.i18n.dummy";

    protected void setUp() throws Exception {
        super.setUp();
        
        ctx = createMock(Context.class);
        // current context locale
        expect(ctx.getLocale()).andReturn(new Locale("en")).anyTimes();
        MgnlContext.setInstance(ctx);

        sysCtx = createStrictMock(SystemContext.class);
        FactoryUtil.setInstance(SystemContext.class, sysCtx);
        FactoryUtil.setDefaultImplementation(MessagesManager.class, DefaultMessagesManager.class);

        // Replace the default bundle (adminterface) by a fake one - see MAGNOLIA-2528
        final DefaultMessagesManager mm = (DefaultMessagesManager) MessagesManager.getInstance();
        mm.setDefaultBasename("info.magnolia.cms.i18n.fakedefault");

        replay(ctx, sysCtx);
    }

    protected void tearDown() throws Exception {
        verify(ctx, sysCtx);
        MgnlContext.setInstance(null);
        FactoryUtil.clear();
        super.tearDown();
    }

    public void testGetsSimpleMessageFromDefaultBundle() {
        assertEquals("Magnolia core tests", MessagesManager.get("about.title"));
        final Messages messages = MessagesManager.getMessages();
        assertEquals("Magnolia core tests", messages.get("about.title"));
    }

    public void testGetsMessagesFromSpecifiedBundle() {
        final Messages messages = MessagesManager.getMessages(DUMMY_BUNDLE);
        assertEquals("Bar", messages.get("foo"));
    }

    public void testGetsMessagesFromSpecifiedBundleInPriorityEvenIfItExistsInDefaultBundle() {
        final Messages messages = MessagesManager.getMessages(DUMMY_BUNDLE);
        assertEquals("This is a test", messages.get("about.title"));
    }

    public void testFallsBackToDefaultLocaleIfCurrentLocaleDoesntSpecifyThisMessage() {
        final Messages messages = MessagesManager.getMessages(DUMMY_BUNDLE, Locale.FRENCH);
        assertEquals("Ceci n'est pas un test", messages.get("about.title"));
        assertEquals("Only in English", messages.get("only.en"));
    }

    public void testFallsBackToDefaultLocaleAlsoWithDefaultBundle() {
        final Messages messages = MessagesManager.getMessages(Locale.FRENCH);
        assertEquals("Autre message seulement defini dans le bundle de base", messages.get("other"));
        assertEquals("Another key only defined in english default bundle", messages.get("another.key"));
    }


}
