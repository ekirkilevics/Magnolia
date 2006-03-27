package info.magnolia.module.owfe.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import openwfe.org.embed.impl.engine.AbstractEmbeddedParticipant;

import org.apache.log4j.Logger;

/**
 * This is used for backward compatibility to access tree and flow commands. We
 * should move to CatalogFactory straight from the calling code.
 * 
 * @author Jackie
 * @author Nicolas
 */
public class CommandsMap {
	static Class klass;

	/**
	 * Logger
	 */
	private static Logger log = Logger
			.getLogger(AbstractEmbeddedParticipant.class);

	// <<<<<<< .mine
	// public MgnlCommand getTreeCommand(String commandName) {
	// HierarchyManager hm = ContentRepository
	// .getHierarchyManager(ContentRepository.CONFIG);
	// try {
	// Content root =
	// hm.getContent("/modules/workflow/config/commands/treeCommands");
	//
	// Content c = root.getContent(commandName);
	// if (c == null) {
	// log.error("can not get command for " + commandName);
	// return null;
	// }
	// String className = c.getNodeData("impl").getString();
	// log.info("class name is " + className);
	// Class cmdClass = Class.forName(className);
	// return (MgnlCommand) cmdClass.newInstance();
	//
	// } catch (javax.jcr.PathNotFoundException pne) {
	// log.warn("command [" + commandName + "] is not defined");
	// return null;
	// }
	// catch (Exception e) {
	// log.warn("can not get command for " + commandName, e);
	// return null;
	// }
	//
	// }
	//
	// public MgnlCommand getFlowCommand(String commandName) {
	// HierarchyManager hm = ContentRepository
	// .getHierarchyManager(ContentRepository.CONFIG);
	// try {
	// Content root =
	// hm.getContent("/modules/workflow/config/commands/flowCommands");
	//
	// Content c = root.getContent(commandName);
	// if (c == null) {
	// log.error("can not get command for " + commandName);
	// return null;
	// }
	// String className = c.getNodeData("impl").getString();
	// log.info("class name is " + className);
	// Class cmdClass = Class.forName(className);
	// return (MgnlCommand) cmdClass.newInstance();
	// } catch (javax.jcr.PathNotFoundException pne) {
	// log.warn("command [" + commandName + "] is not defined");
	// return null;
	// } catch (Exception e) {
	// log.warn("can not get command for " + commandName, e);
	// return null;
	// }
	//
	// }

	// public MgnlCommand getCommand(String commandName) {
	// HierarchyManager hm = ContentRepository
	// .getHierarchyManager(ContentRepository.CONFIG);
	// =======
	static {
		// >>>>>>> .r2446
		try {
			// <<<<<<< .mine
			// Content root =
			// hm.getContent("/modules/workflow/config/commands");
			//
			// Content c = root.getContent(commandName);
			// if (c == null) {
			// log.error("can not get command for " + commandName);
			// return null;
			// }
			// String className = c.getNodeData("impl").getString();
			// log.info("class name is " + className);
			// Class cmdClass = Class.forName(className);
			// return (MgnlCommand) cmdClass.newInstance();
			// } catch (javax.jcr.PathNotFoundException pne) {
			// log.warn("command [" + commandName + "] is not defined");
			// return null;
			// =======
			klass = Class
					.forName("info.magnolia.cms.beans.commands.MgnlCatalogFactory");
			// >>>>>>> .r2446
			// <<<<<<< .mine
			// } catch (Exception e) {
			// log.warn("can not get command for " + commandName, e);
			// return null;
			// }
			//
			// =======
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public MgnlCommand getCommand(/*String catalogName, */String commandName)
	// {
	// CatalogFactory factory = (CatalogFactory)
	// FactoryUtil.getSingleton(klass);
	// //Catalog catalog = factory.getCatalog(catalogName);
	// Catalog catalog = factory.getCatalog("");
	// return (MgnlCommand) catalog.getCommand(commandName);
	// //>>>>>>> .r2446
	// }

	public MgnlCommand getCommand(String commandName) {
		HierarchyManager hm = ContentRepository
				.getHierarchyManager(ContentRepository.CONFIG);

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
