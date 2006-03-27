package info.magnolia.module.owfe.commands;

import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;

/**
 * This is used for backward compatibility to access tree and flow commands. We should move to CatalogFactory straight
 * from the calling code.
 *
 * @author Jackie
 * @author Nicolas
 */
public class CommandsMap {
    static Class klass;

    static {
        try {
            klass = Class.forName("info.magnolia.cms.beans.commands.MgnlCatalogFactory");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MgnlCommand getTreeCommand(String commandName) {
        return getCommand("treeCommands", commandName);
    }

    public MgnlCommand getFlowCommand(String commandName) {
        return getCommand("flowCommands", commandName);
    }

    private MgnlCommand getCommand(String catalogName, String commandName) {
        CatalogFactory factory = (CatalogFactory) FactoryUtil.getSingleton(klass);
        Catalog catalog = factory.getCatalog(catalogName);
        return (MgnlCommand) catalog.getCommand(commandName);
    }

}
