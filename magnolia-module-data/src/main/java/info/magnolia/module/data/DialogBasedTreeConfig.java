package info.magnolia.module.data;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.control.ContextMenu;
import info.magnolia.cms.gui.control.ContextMenuItem;
import info.magnolia.cms.gui.control.FunctionBar;
import info.magnolia.cms.gui.control.FunctionBarItem;
import info.magnolia.cms.gui.control.Tree;
import info.magnolia.cms.gui.control.TreeColumn;
import info.magnolia.cms.gui.control.TreeColumnHtmlRenderer;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeConfiguration;
import info.magnolia.module.admininterface.dialogs.ConfiguredDialog;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class DialogBasedTreeConfig implements AdminTreeConfiguration {

	protected final ConfiguredDialog dialog;
	protected final String repository;
	
	public DialogBasedTreeConfig(final ConfiguredDialog config, final String repository){
		this.dialog = config;
		this.repository = repository;
	}
	
	
	
	public void prepareTree(Tree tree, boolean browseMode, HttpServletRequest request) {
        tree.addItemType(dialog.getConfigNode().getNodeData("itemType").getString());

        Collection controls = ContentUtil.collectAllChildren(dialog.getConfigNode(), new Content.ContentFilter(){
        	public boolean accept(Content content) {
        		final NodeData control = content.getNodeData("controlType");
        		return control != null && !control.getString().equalsIgnoreCase("tab") && !StringUtils.isEmpty(control.getString());
        	}
        });
        for(Iterator it = controls.iterator(); it.hasNext();){
        	final Content field = (Content)it.next();
            TreeColumn col = new TreeColumn(tree.getJavascriptTree(), request);
            col.setWidth(1);
            col.setName(field.getNodeData("name").getString());
//            col.setIsLabel(true);
            col.setTitle(field.getName());
        	if(field.getNodeData("controlType").getString().equals("referenceMultiSelect")) {
        		col.setHtmlRenderer(new TreeColumnHtmlRenderer() {
        			public String renderHtml(TreeColumn treeColumn, Content content) {
        				try {
            				Collection uuids = content.getContent(treeColumn.getName()).getNodeDataCollection();
            				final StringBuffer result = new StringBuffer();
            				final HierarchyManager hm = MgnlContext.getHierarchyManager(repository);
            				for(Iterator it = uuids.iterator(); it.hasNext();) {
            					final NodeData uuid = (NodeData)it.next();
            					result.append(hm.getContentByUUID(uuid.getString()).getHandle()).append(", ");
            				}
            				if(result.length() > 1){
            					result.setLength(result.length()-2);
            				}
							return result.toString();
						} catch (AccessDeniedException e) {
							e.printStackTrace();
						} catch (PathNotFoundException e) {
							e.printStackTrace();
						} catch (RepositoryException e) {
							e.printStackTrace();
						}
						return "??";
        			}
        		});
        	}
            tree.addColumn(col);
        }
	}

	public void prepareContextMenu(Tree tree, boolean browseMode, HttpServletRequest request) {
        ContextMenuItem menuNewType = new ContextMenuItem("new");
        menuNewType.setLabel("data.menu.new");
        menuNewType.setIcon(request.getContextPath() + "/.resources/icons/16/document_add.gif");
        menuNewType.setOnclick("mgnl.data.Data.createNew(" + tree.getJavascriptTree() + ".selectedNode.id, '"+dialog.getConfigNode().getName()+"');");
        menuNewType.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotContent("
            + tree.getJavascriptTree()
            + ")");
        
        tree.addMenuItem(menuNewType);

        ContextMenuItem menuEditType = new ContextMenuItem("edit");
        menuEditType.setLabel("data.menu.edit");
        menuEditType.setIcon(request.getContextPath() + "/.resources/icons/16/document_edit.gif");
        menuEditType.setOnclick("mgnlTreeMenuOpenDialog("
            + tree.getJavascriptTree()
            + ",'.magnolia/dialogs/"+dialog.getConfigNode().getName()+".html');");
        menuEditType.addJavascriptCondition("new mgnlTreeMenuItemConditionSelectedNotRoot(" //$NON-NLS-1$
            + tree.getJavascriptTree() + ")"); //$NON-NLS-1$

        tree.addMenuItem(menuEditType);
	}

	public void prepareFunctionBar(Tree tree, boolean browseMode, HttpServletRequest request) {
        FunctionBar bar = tree.getFunctionBar();
        ContextMenu menu = tree.getMenu();
        bar.setSearchable(false);
        
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("new")));
        bar.addMenuItem(new FunctionBarItem(menu.getMenuItemByName("edit")));
	}

}
