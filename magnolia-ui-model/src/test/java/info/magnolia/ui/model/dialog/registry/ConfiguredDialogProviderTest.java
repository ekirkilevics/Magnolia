/**
 * This file Copyright (c) 2010-2011 Magnolia International
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
package info.magnolia.ui.model.dialog.registry;

/*import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import info.magnolia.importexport.PropertiesImportExport;
import info.magnolia.test.ComponentsTestUtil;
*/
import org.junit.Test;

import info.magnolia.test.RepositoryTestCase;

/*import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
*/
/**
 * Tests for Dialog C2B.
 *
 * @author had
 * @version $Id:$
 */
public class ConfiguredDialogProviderTest extends RepositoryTestCase {
    @Test
    public void testDummy() throws Exception {

    }
   /* private HierarchyManager hm;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        String dummyDialog =
            "/modules.@type=mgnl:content\n" +
            "/modules/test.@type=mgnl:content\n" +
            "/modules/test/dialogs.@type=mgnl:content\n" +
            "/modules/test/dialogs/dummyDialog.@type=mgnl:content\n" +
            "/modules/test/dialogs/dummyDialog/tabs.@type=mgnl:contentNode\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main.@type=mgnl:contentNode\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main.label=TestDialog\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main/fields.@type=mgnl:contentNode\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main/fields/text.@type=mgnl:contentNode\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main/fields/text/class=info.magnolia.ui.admincentral.control.EditControl\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main/fields/link.@type=mgnl:contentNode\n" +
            "/modules/test/dialogs/dummyDialog/tabs/main/fields/link/class=info.magnolia.ui.admincentral.control.LinkControl";

        hm = MgnlContext.getHierarchyManager(ContentRepository.CONFIG);
        new PropertiesImportExport().createContent(hm.getRoot(), IOUtils.toInputStream(dummyDialog));
        hm.save();
    }

    @After
    public void tearDown() throws Exception {
        ComponentsTestUtil.clear();
        super.tearDown();
    }

    @Test
    public void testGetDialogDefinition() throws Exception {
        DialogDefinition definition = new ConfiguredDialogProvider(hm.getContent("/modules/test/dialogs/dummyDialog")).getDialogDefinition();
        assertEquals(1, definition.getTabs().size());
        DialogTab tab = definition.getTabs().get(0);
        assertEquals("TestDialog", tab.getLabel());
        assertEquals(2, tab.getFields().size());
    }*/
}
