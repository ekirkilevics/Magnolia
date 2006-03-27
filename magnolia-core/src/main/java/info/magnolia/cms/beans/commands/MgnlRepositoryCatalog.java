package info.magnolia.cms.beans.commands;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import org.apache.commons.chain.Command;

import java.util.Iterator;

/**
 * Date: Mar 27, 2006
 * Time: 10:58:22 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlRepositoryCatalog extends MgnlBaseCatalog {

    static final String REPO_PATH = "/modules/workflow/config/commands";

    public void initCatalog(String name) {
        try {
            Context context = MgnlContext.getSystemContext();
            HierarchyManager hm = context.getHierarchyManager("config");
            Content content = hm.getContent(REPO_PATH + "/" + name);
            Iterator iter = content.getChildren(ItemType.CONTENTNODE).iterator();
            while (iter.hasNext()) {
                Content actionNode = (Content) iter.next();
                String actionName = actionNode.getName();
                String className = actionNode.getNodeData("impl").getString();
                Class klass = Class.forName(className);
                Command command = (Command) klass.newInstance();
                if (log.isDebugEnabled())
                    log.debug("Found new command for catalog:[" + name + "]: [ [" + actionName + "]@[" + className + "] ]");
                this.addCommand(actionName, command);
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
