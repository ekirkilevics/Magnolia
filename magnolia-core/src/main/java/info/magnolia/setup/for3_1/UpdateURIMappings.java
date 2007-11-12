/**
 * 
 */
package info.magnolia.setup.for3_1;

import info.magnolia.cms.beans.config.DefaultVirtualURIMapping;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AllModulesNodeOperation;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.RepositoryException;

/**
 * @author vsteller
 *
 */
public class UpdateURIMappings extends AllModulesNodeOperation {

	public UpdateURIMappings() {
		super("Update virtualURIMapping nodes", "Adds the class property to each virtualURIMapping node as these are dynamic now.");
	}

	/* (non-Javadoc)
	 * @see info.magnolia.module.delta.AllModulesNodeOperation#operateOnModuleNode(info.magnolia.cms.core.Content, info.magnolia.cms.core.HierarchyManager, info.magnolia.module.InstallContext)
	 */
	protected void operateOnModuleNode(Content node, HierarchyManager hm,
			InstallContext ctx) throws RepositoryException,
			TaskExecutionException {

        try {
            if(node.hasContent("virtualURIMapping")){
                ContentUtil.visit(node.getContent("virtualURIMapping"), new ContentUtil.Visitor(){
                   public void visit(Content node) throws Exception {
                       if(node.hasNodeData("fromURI") && node.hasNodeData("toURI")){
                           NodeData classNodeData = NodeDataUtil.getOrCreate(node, "class");
                           classNodeData.setValue(DefaultVirtualURIMapping.class.getName());
                       }
                   }
                });
            }
        }
        catch(RepositoryException e){
            throw e;
        }
        catch (Exception e) {
            throw new TaskExecutionException("can't reconfigure virtualURIMapping", e);
        }
	}

}
