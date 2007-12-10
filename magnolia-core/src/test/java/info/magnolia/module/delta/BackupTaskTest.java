/**
 * This file Copyright (c) 2007 Magnolia International
 * Ltd.  (http://www.magnolia.info). All rights reserved.
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
 * is available at http://www.magnolia.info/mna.html
 * 
 * Any modifications to this file must keep this entire header
 * intact.
 * 
 */
package info.magnolia.module.delta;

import static org.easymock.EasyMock.*;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.test.MgnlTestCase;
import info.magnolia.test.mock.MockUtil;

import java.util.Properties;


/**
 * @author vsteller
 * @version $Id$
 *
 */
public class BackupTaskTest extends MgnlTestCase {
    
    private final String backupPath = "/backup/of";

    public void testNodeExistsWithFullPathAtBackupPathAfterBackingUp() throws Exception {
        // TODO: not testable yet, since MoveNodeTask will throw a NPE due to MAGNOLIA-1951
//        final String testContent = "" +
//            "/original/path/to/node.someProperty=someValue";
//        final String workspace = "config";
//        final HierarchyManager hm = MockUtil.createHierarchyManager(testContent);
//        final InstallContext ctx = createStrictMock(InstallContext.class);
//        expect(ctx.getHierarchyManager(workspace)).andReturn(hm).atLeastOnce();
//        
//        replay(ctx);
//        final BackupTask task = new BackupTask(workspace, "/original/path/to/node") {
//            protected String getBackupPath() {
//                return backupPath;
//            }
//        };
//        task.execute(ctx);
//        verify(ctx);
//        
//        final Properties hmProps = MockUtil.toProperties(hm);
//        assertEquals(hmProps.get("/backup/of/original/path/to/node.someProperty"), "someValue");
    }
}
