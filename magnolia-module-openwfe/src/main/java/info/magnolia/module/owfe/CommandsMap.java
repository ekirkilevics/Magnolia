package info.magnolia.module.owfe;

import java.util.Collection;
import java.util.Iterator;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;

import org.apache.log4j.Logger;

public class CommandsMap {
	/**
	 * Logger
	 */
	private static Logger log = Logger.getLogger(CommandsMap.class);

	public Command getTreeCommand(String commandName) {
		HierarchyManager hm = hm = ContentRepository
				.getHierarchyManager(ContentRepository.CONFIG);
		try {
			Content root = hm.getContent("/commands/treeCommands");

			Content c = root.getContent(commandName);
			if (c == null) {
				log.error("can not get command for " + commandName);
				return null;
			}
			String className = c.getNodeData("impl").getString();
			log.info("class name is " + className);
			Class cmdClass = Class.forName(className);
			Command ret = (Command)cmdClass.newInstance();
			return ret;
			
		} catch (Exception e) {
			log.warn("can not get command for " + commandName, e);
			return null;
		}
		
	}
	public Command getFlowCommand(String commandName) {
		HierarchyManager hm = hm = ContentRepository
				.getHierarchyManager(ContentRepository.CONFIG);
		try {
			Content root = hm.getContent("/commands/flowCommands");

			Content c = root.getContent(commandName);
			if (c == null) {
				log.error("can not get command for " + commandName);
				return null;
			}
			String className = c.getNodeData("impl").getString();
			log.info("class name is " + className);
			Class cmdClass = Class.forName(className);
			Command ret = (Command)cmdClass.newInstance();
			return ret;
			
		} catch (Exception e) {
			log.warn("can not get command for " + commandName, e);
			return null;
		}
		
	}

}
