/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.setup.for3_1;

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

        final Properties hmAsProps = MockUtil.toProperties(hm);
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
