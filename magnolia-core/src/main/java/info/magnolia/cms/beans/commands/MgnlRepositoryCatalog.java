package info.magnolia.cms.beans.commands;

import info.magnolia.cms.beans.runtime.Context;
import info.magnolia.cms.beans.runtime.MgnlContext;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.impl.ChainBase;
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


    public void initCatalog(String name) {
    	String path = null;
    	if (name == null || name.length()==0)
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

                // consider any command as a chain, makes things easier
                // we iterate through each subnode and consider this as a command in the chain
                Collection childrens = actionNode.getChildren(ItemType.CONTENTNODE);
                log.info("Found " + childrens.size() + " children for action " + actionName);
                Iterator iterNode = childrens.iterator();
                Chain chain = new ChainBase();
                String className = StringUtils.EMPTY;
                Exception e = null;
                while (iterNode.hasNext()) {
                    className = StringUtils.EMPTY;
                    try {
                        Content commandNode = (Content) iterNode.next();
                        className = commandNode.getNodeData("className").getString();
                        log.info("Found class " + className + " for action " + actionName);
                        Class klass = Class.forName(className);
                        Command command = (Command) klass.newInstance();
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
        catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
