package info.magnolia.module.owfe.commands.simple;

import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.module.owfe.commands.MgnlCommand;
import openwfe.org.engine.workitem.InFlowWorkItem;
import org.apache.commons.chain.Context;
import org.apache.commons.lang.StringUtils;

import javax.jcr.RepositoryException;
import java.util.HashMap;

public class DeleteCommand extends SimpleCommand{

	public boolean exec(HashMap params, Context ctx) {
 
      String path;

       path = (String) params.get(P_PATH);
  
      try {
          deleteNode(ctx, path);
      } catch (Exception e) {
          log.error("cannot do delete", e);
          return false;
      }
      return true;

	}

//	public boolean execute(Context context) {
//        HashMap params = (HashMap) context.get(PARAMS);
//        String path;
//        InFlowWorkItem if_wi = (InFlowWorkItem) params.get(MgnlCommand.P_WORKITEM);
//        if (if_wi != null) { // if call from flow
//            path = (if_wi.getAttribute(P_PATH)).toString();
//        } else {
//            path = (String) params.get(P_PATH);
//        }
//        try {
//            deleteNode(context, path);
//        } catch (Exception e) {
//            log.error("cannot do delete", e);
//            return false;
//        }
//        return true;
//    }

    private void deleteNode(Context context, String parentPath, String label) throws RepositoryException {
        Content parentNode = MgnlContext.getHierarchyManager(MgnlCommand.REPOSITORY).getContent(parentPath);
        String path;
        if (!parentPath.equals("/")) { //$NON-NLS-1$
            path = parentPath + "/" + label; //$NON-NLS-1$
        } else {
            path = "/" + label; //$NON-NLS-1$
        }
        ((HashMap) context.get(PARAMS)).put(P_PATH, path);
        new DeactivationCommand().execute(context);
        parentNode.delete(label);
        parentNode.save();
    }

    private void deleteNode(Context context, String path) throws Exception {
        String parentPath = StringUtils.substringBeforeLast(path, "/"); //$NON-NLS-1$
        String label = StringUtils.substringAfterLast(path, "/"); //$NON-NLS-1$
        deleteNode(context, parentPath, label);
    }

}
