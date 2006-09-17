package info.magnolia.module.workflow.trees;

import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.module.admininterface.trees.ConfigTreeConfiguration;

import javax.servlet.http.HttpServletRequest;

/**
 * Tree to view workitems and expressions
 */
public class WorkflowTreeConfiguration extends ConfigTreeConfiguration {

    public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        super.prepareTree(tree,browseMode,request);
        tree.addItemType(ItemType.WORKITEM);
        tree.addItemType(ItemType.NT_UNSTRUCTRUED);
        tree.addItemType(ItemType.EXPRESSION);
    }

}
