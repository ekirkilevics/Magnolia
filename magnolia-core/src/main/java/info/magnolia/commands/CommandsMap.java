package info.magnolia.commands;

import info.magnolia.cms.util.FactoryUtil;
import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is used for backward compatibility to access tree and flow commands. We
 * should move to CatalogFactory straight from the calling code.
 *
 * @author Jackie
 * @author Nicolas
 */
public class CommandsMap {

    private static Logger log = LoggerFactory.getLogger(CommandsMap.class);

    static Class klass;

    static {
        try {

            klass = Class
                    .forName("info.magnolia.commands.MgnlCatalogFactory");
        } catch (Exception e) {
            log.error("Could not load command factory", e);
        }
    }

    public static MgnlCommand getCommand(String catalogName, String commandName) {
        CatalogFactory factory = (CatalogFactory) FactoryUtil
                .getSingleton(klass);
        Catalog catalog = factory.getCatalog(catalogName);
        return (MgnlCommand) catalog.getCommand(commandName);
    }

    public static MgnlCommand getCommand(String commandName) {
        CatalogFactory factory = (CatalogFactory) FactoryUtil
                .getSingleton(klass);
        Catalog catalog = factory.getCatalog("");
        return (MgnlCommand) catalog.getCommand(commandName);
    }

    public static MgnlCommand getCommandFromFullName(String commandName) {
        String command_ = commandName.substring(MgnlCommand.PREFIX_COMMAND_LEN);
        int index1 = command_.indexOf(MgnlCommand.COMMAND_DELIM);
        int index2 = command_.indexOf(MgnlCommand.COMMAND_DELIM, index1);
        String catalog;
        String name;
        if (index2 == -1) {
            catalog = MgnlCommand.DEFAULT_CATALOG;
            name = command_.substring(index1 + 1);
        } else {
            catalog = command_.substring(index1 + 1, index2);
            name = command_.substring(index2 + 1);
        }
        MgnlCommand command = getCommand(catalog, name);
        if (command == null)
            command = getCommand(MgnlCommand.DEFAULT_CATALOG, name);
        return command;
    }

}
