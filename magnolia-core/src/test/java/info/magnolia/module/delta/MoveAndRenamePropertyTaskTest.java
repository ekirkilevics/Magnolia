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
