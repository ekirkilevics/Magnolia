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
package info.magnolia.commands;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import info.magnolia.cms.util.ContentUtil;
import info.magnolia.commands.chain.Catalog;
import info.magnolia.commands.chain.Command;
import info.magnolia.commands.chain.Context;
import info.magnolia.context.SystemContext;
import info.magnolia.objectfactory.Components;
import info.magnolia.test.ComponentsTestUtil;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.jcr.MockSession;
import info.magnolia.test.mock.jcr.SessionTestUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.jcr.Node;

import org.junit.Before;
import org.junit.Test;

/**
 * CommandsManagerTest.
 */
public class CommandsManagerTest extends MgnlTestCase {
    private String config = "/modules.uuid=1\n" +
            "/modules/mymodule/commands/default/foo.class=info.magnolia.commands.TestMgnlCommand\n" +
            "/modules/mymodule/commands/default/bar.class=info.magnolia.commands.TestMgnlCommand\n" +
            "/modules/mymodule2/commands/baz/bar.class=info.magnolia.commands.TestMgnlCommand\n" +
            "/modules/mymodule2/commands/baz/foo.class=info.magnolia.commands.TestMgnlCommand\n";

    private CommandsManager commandsManager;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        MockSession session = SessionTestUtil.createSession("config", config);

        SystemContext systemContext = mock(SystemContext.class);
        when(systemContext.getJCRSession("config")).thenReturn(session);
        ComponentsTestUtil.setInstance(SystemContext.class, systemContext);

        ComponentsTestUtil.setImplementation(CommandsManager.class, CommandsManager.class);
        // see for why this is needed.
        ComponentsTestUtil.setInstance(Map.class, new LinkedHashMap());

        commandsManager = Components.getComponent(CommandsManager.class);
        Node mymodule = session.getNode("/modules/mymodule/commands/default");
        commandsManager.register(ContentUtil.asContent(mymodule));

        Node mymodule2 = session.getNode("/modules/mymodule2/commands/baz");
        commandsManager.register(ContentUtil.asContent(mymodule2));
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testGetCommandByName() throws Exception {
        // GIVEN see setup
        Catalog catalog = commandsManager.getCatalogByName("baz");

        // WHEN
        Command bar = catalog.getCommand("bar");

        // THEN
        assertNotNull(bar);

        // WHEN
        Command foo = catalog.getCommand("foo");

        // THEN
        assertNotNull(foo);

        // GIVEN
        catalog = commandsManager.getCatalogByName("default");

        // WHEN
        Command defaultBar = catalog.getCommand("bar");

        // THEN
        assertNotNull(defaultBar);

        // WHEN
        Command defaultFoo = catalog.getCommand("foo");

        // THEN
        assertNotNull(defaultFoo);
    }

    @Test
    public void testRegisterCommands() throws Exception {
        //GIVEN see setup

        //WHEN
        Catalog defaultCatalog = commandsManager.getCatalogByName("default");

        // THEN
        assertEquals(2, getCatalogSize(defaultCatalog));

        // WHEN
        Catalog bazCatalog = commandsManager.getCatalogByName("baz");

        // THEN
        assertEquals(2, getCatalogSize(bazCatalog));
    }

    @Test
    public void testGetCommandByCatalogAndName() throws Exception {
        // GIVEN see setup

        // WHEN
        Command defaultFoo = commandsManager.getCommand("default", "foo");
        Command bazFoo = commandsManager.getCommand("baz", "foo");

        // THEN
        assertNotNull(defaultFoo);
        assertNotNull(bazFoo);
        assertNotEquals(defaultFoo, bazFoo);

    }

    @Test
    public void testExecuteCommand() throws Exception {
        // GIVEN see setup
        Map<String,Object> params = new HashMap<String, Object>();
        params.put("mustRead", "Die Welt als Wille und Vorstellung");

        TestMgnlCommand testCommand = (TestMgnlCommand) commandsManager.getCommand("default", "foo");
        assertNull(testCommand.getContext());

        // WHEN
        commandsManager.executeCommand(testCommand, params);

        // THEN
        Context ctx = testCommand.getContext();
        assertNotNull(ctx);
        assertEquals(ctx.get("mustRead"), "Die Welt als Wille und Vorstellung");

    }

    private int getCatalogSize(Catalog catalog) {
        int i = 0;
        Iterator iterator = catalog.getNames();
        while (iterator.hasNext()) {
            iterator.next();
            i++;
        }
        return i;
    }
}
