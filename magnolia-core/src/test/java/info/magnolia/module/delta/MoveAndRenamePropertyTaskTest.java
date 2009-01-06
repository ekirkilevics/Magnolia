/**
 * This file Copyright (c) 2003-2009 Magnolia International
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

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.module.InstallContext;
import junit.framework.TestCase;
import static org.easymock.EasyMock.*;

import javax.jcr.RepositoryException;

/**
 * TODO : tests when the parent nodes are not existing.
 *
 * @author gjoseph
 * @version $Revision: $ ($Author: $)
 */
public class MoveAndRenamePropertyTaskTest extends TestCase {
    private InstallContext ctx;
    private HierarchyManager hm;
    private Content origParent;
    private Content newParent;
    private NodeData origProp;
    private NodeData newProp;
    private MoveAndRenamePropertyTask task;


    protected void setUp() throws Exception {
        super.setUp();
        ctx = createStrictMock(InstallContext.class);
        hm = createStrictMock(HierarchyManager.class);
        origParent = createStrictMock(Content.class);
        newParent = createStrictMock(Content.class);
        origProp = createStrictMock(NodeData.class);
        newProp = createStrictMock(NodeData.class);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        replay(ctx, hm, origParent, origProp, newParent, newProp);
        task.execute(ctx);
        verify(ctx, hm, origParent, origProp, newParent, newProp);
    }

    public void testPropertyWithOldDefaultValueGetsNewDefaultValue() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", "old-default", "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn("old-default");
        expect(origProp.isExist()).andReturn(true);
        origProp.delete();
        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(false);
        expect(newParent.createNodeData("newprop", "new-default")).andReturn(newProp);
    }

    public void testPropertyWithDifferentValueIsMovedButNotChanged() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", "old-default", "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn("custom-value");
        expect(origProp.isExist()).andReturn(true);
        origProp.delete();
        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(false);
        expect(newParent.createNodeData("newprop", "custom-value")).andReturn(newProp);
    }

    public void testValueIsCopiedNoMatterWhatIfOldDefaultNotPassed() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", null, "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn("custom-value");
        expect(origProp.isExist()).andReturn(true);
        origProp.delete();
        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(false);
        expect(newParent.createNodeData("newprop", "custom-value")).andReturn(newProp);
    }

    public void testNewDefaultIsUsedIfPropertyDidNotExist() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", "oldvalue", "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn(null);
        expect(origProp.isExist()).andReturn(false);

        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(false);
        expect(newParent.createNodeData("newprop", "new-default")).andReturn(newProp);
    }

    public void testNewDefaultIsUsedIfPropertyDidNotExistAndNodeDataReturnsEmptyValue() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", "oldvalue", "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn("");
        expect(origProp.isExist()).andReturn(false);

        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(false);
        expect(newParent.createNodeData("newprop", "new-default")).andReturn(newProp);
    }

    public void testPropertyReplacedByOldValueIfNewAlreadyExists() throws RepositoryException {
        task = new MoveAndRenamePropertyTask("", "/foo", "oldprop", "old-default", "/bar", "newprop", "new-default");

        expect(ctx.getConfigHierarchyManager()).andReturn(hm);
        expect(hm.isExist("/foo")).andReturn(true);
        expect(hm.getContent("/foo")).andReturn(origParent);
        expect(origParent.getNodeData("oldprop")).andReturn(origProp);
        expect(origProp.getString()).andReturn("custom-value");
        expect(origProp.isExist()).andReturn(true);
        origProp.delete();
        expect(hm.isExist("/bar")).andReturn(true);
        expect(hm.getContent("/bar")).andReturn(newParent);
        expect(newParent.hasNodeData("newprop")).andReturn(true);
        expect(newParent.getNodeData("newprop")).andReturn(newProp);
        expect(newProp.getString()).andReturn("boobaa"); // log message
        ctx.info("Replacing property newprop at /bar with value custom-value. Previous value was boobaa");
        newProp.setValue("custom-value");
    }

}
