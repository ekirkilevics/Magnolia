package info.magnolia.commands;

import info.magnolia.cms.beans.commands.MgnlCommand;
import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Iterator;

/**
 * Date: Mar 27, 2006
 * Time: 10:58:22 AM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MgnlRepositoryCatalog extends MgnlBaseCatalog {

    static final String REPO_PATH = "/modules/workflow/config/commands";
    static final String CLASS_NODE_DATA = "impl";


    public void initCatalog(String name) {
        String path;
        if (name == null || name.length() == 0)
            path = REPO_PATH;
        else
            path = REPO_PATH + "/" + name;

        try {
            Context context = MgnlContext.getSystemContext();
            HierarchyManager hm = context.getHierarchyManager("config");
            Content content = hm.getContent(path);
            Iterator iter = content.getChildren(ItemType.CONTENTNODE).iterator();
            // loop over the command names one by one
            while (iter.hasNext()) {
                // get the action node and name
                Content actionNode = (Content) iter.next();
                String actionName = actionNode.getName();

                String className = StringUtils.EMPTY;

                NodeData impl = actionNode.getNodeData(CLASS_NODE_DATA);
                if (impl != null && impl.getString() != null && !(impl.getString().equals(""))) {
                    if (log.isDebugEnabled())
                        log.debug("This is a simple action" + actionName);
                    // this is a simple command
                    className = impl.getString();
                    Class klass = Class.forName(className);
                    try {
                        this.addCommand(actionName, (Command) klass.newInstance());
                    }
                    catch (Exception e) {
                        log.error("Could not load action:" + actionName, e);
                    }
                    // continue with next action
                } else {
                    if (log.isDebugEnabled())
                        log.debug("This is a chain");
                    // this is a chain
                    Chain chain = new MgnlChain();

                    // consider any command as a chain, makes things easier
                    // we iterate through each subnode and consider this as a command in the chain
                    Collection childrens = actionNode.getChildren(ItemType.CONTENTNODE);
                    if (log.isDebugEnabled())
                        log.debug("Found " + childrens.size() + " children for action " + actionName);
                    Iterator iterNode = childrens.iterator();

                    Exception e = null;
                    while (iterNode.hasNext()) {
                        try {
                            Content commandNode = (Content) iterNode.next();
                            className = commandNode.getNodeData(CLASS_NODE_DATA).getString();
                            if (log.isDebugEnabled())
                                log.debug("Found class " + className + " for action " + actionName);
                            Class klass = Class.forName(className);
                            MgnlCommand command = (MgnlCommand) klass.newInstance();
                            chain.addCommand(command);
                        }
                        catch (Exception te) {
                            e = te;
                            break;
                        }
                    }

                    // add the command only if no error was reported
                    if (e != null)
                        log.error("Could not load commands for action:" + actionName, e);
                    else   // add the chain to the catalog linked with the actionName
                        this.addCommand(actionName, chain);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }
}