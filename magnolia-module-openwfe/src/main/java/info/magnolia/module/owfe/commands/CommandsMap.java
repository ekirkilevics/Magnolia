package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.FactoryUtil;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;
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

    static final String COMMAND_PREFIX = "command-";
    static final int COMMAND_PREFIX_LEN = COMMAND_PREFIX.length();

    private static Logger log = Logger.getLogger(AbstractEmbeddedParticipant.class);

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

    public static MgnlCommand getCommandFromFullName(String commandName) {
        String command_ = commandName.substring(COMMAND_PREFIX_LEN);
        int index1 = command_.indexOf("-");
        int index2 = command_.indexOf("-", index1);
        String catalog = command_.substring(index1 + 1, index2);
        String name = command_.substring(index2 + 1);
        return getCommand(catalog, name);
    }


    public static MgnlCommand getCommand(String commandName) {
        HierarchyManager hm = ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
        try {

            Content root = hm.getContent("/modules/workflow/config/commands");
            Content c = root.getContent(commandName);
            if (c == null) {
                log.error("can not get command for " + commandName);
                return null;
            }
            String className = c.getNodeData("impl").getString();
            log.info("class name is " + className);
            Class cmdClass = Class.forName(className);
            return (MgnlCommand) cmdClass.newInstance();
        } catch (javax.jcr.PathNotFoundException pne) {
            log.warn("command [" + commandName + "] is not defined");
            return null;

        } catch (Exception e) {
            log.warn("can not get command for " + commandName, e);
            return null;
        }

    }

}
