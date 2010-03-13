/**
 * This file Copyright (c) 2003-2010 Magnolia International
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
package info.magnolia.setup.for3_5;

import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.module.InstallContext;
import info.magnolia.test.mock.MockHierarchyManager;
import info.magnolia.test.mock.MockUtil;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import java.util.Properties;

/**
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class IPConfigRulesUpdateTest extends TestCase {
    public void testOnlyChangesOldStyleRulesOrdersMethodsUppercasesThemAndLeavesOtherNodesUntouched() throws Exception {
        final String testContent = "" +
                "server.IPConfig.allow-all.IP=*\n" +
                "server.IPConfig.allow-all.Access.0001.Method=GET\n" +
                "server.IPConfig.allow-all.Access.0002.Method=POST\n" +
                "server.IPConfig.allow-all.Access.0003.Method=GET\n" +
                "server.IPConfig.someNewRule.IP=192.168.0.1\n" +
                "server.IPConfig.someNewRule.methods=GET,POST,PUT\n" +
                "server.IPConfig.other-old-rule.IP=127.0.0.1\n" +
                "server.IPConfig.other-old-rule.Access.0001.Method=SPACEJUMP\n" +
                "server.IPConfig.other-old-rule.Access.0002.Method=get\n" +
                "server.IPConfig.other-old-rule.Access.000x.Method=delEte\n" +
                "server.IPConfig.other-old-rule.Access.0003.Method=LINK\n" +
                "server.IPConfig.foo.bar=baz\n";

        final MockHierarchyManager hm = MockUtil.createHierarchyManager(testContent);
        final InstallContext ctx = createStrictMock(InstallContext.class);
        expect(ctx.getHierarchyManager("config")).andReturn(hm);

        replay(ctx);
        final IPConfigRulesUpdate task = new IPConfigRulesUpdate();
        task.execute(ctx);
        verify(ctx);

        final Properties hmAsProps = PropertiesImportExport.toProperties(hm);
        assertEquals(7, hmAsProps.size());
        assertEquals("*", hmAsProps.get("/server/IPConfig/allow-all.IP"));
        assertEquals("192.168.0.1", hmAsProps.get("/server/IPConfig/someNewRule.IP"));
        assertEquals("127.0.0.1", hmAsProps.get("/server/IPConfig/other-old-rule.IP"));
        assertEquals("GET,POST", hmAsProps.get("/server/IPConfig/allow-all.methods"));
        assertEquals("GET,POST,PUT", hmAsProps.get("/server/IPConfig/someNewRule.methods"));
        assertEquals("DELETE,GET,LINK,SPACEJUMP", hmAsProps.get("/server/IPConfig/other-old-rule.methods"));
        assertEquals("baz", hmAsProps.get("/server/IPConfig/foo.bar"));
    }
}
