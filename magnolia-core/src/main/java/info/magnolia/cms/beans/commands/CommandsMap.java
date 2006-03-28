package info.magnolia.cms.beans.commands;

import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.log4j.Logger;

/**
 * This is used for backward compatibility to access tree and flow commands. We
 * should move to CatalogFactory straight from the calling code.
 *
 * @author Jackie
 * @author Nicolas
 */
public class CommandsMap {

    private static Logger log = Logger.getLogger(CommandsMap.class);

    static Class klass;

    static {
        try {

            klass = Class.forName("info.magnolia.cms.beans.commands.MgnlCatalogFactory");
        } catch (Exception e) {
            log.error("Could not load command factory", e);
        }
    }

    public static MgnlCommand getCommand(String catalogName, String commandName) {
        CatalogFactory factory = (CatalogFactory) FactoryUtil.getSingleton(klass);
        Catalog catalog = factory.getCatalog(catalogName);
        return (MgnlCommand) catalog.getCommand(commandName);
    }

    public static MgnlCommand getCommand(String commandName) {
        CatalogFactory factory = (CatalogFactory) FactoryUtil.getSingleton(klass);
        Catalog catalog = factory.getCatalog("");
        return (MgnlCommand) catalog.getCommand(commandName);
    }

    public static MgnlCommand getCommandFromFullName(String commandName) {
        String command_ = commandName.substring(MgnlCommand.PREFIX_COMMAND_LEN);
        int index1 = command_.indexOf(MgnlCommand.COMMAND_DELIM);
        int index2 = command_.indexOf(MgnlCommand.COMMAND_DELIM, index1);
        String catalog = command_.substring(index1 + 1, index2);
        String name = command_.substring(index2 + 1);
        return getCommand(catalog, name);
    }

}
