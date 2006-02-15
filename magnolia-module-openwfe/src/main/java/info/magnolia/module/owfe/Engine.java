/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2005 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */
package info.magnolia.module.owfe;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.beans.config.Template;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.module.Module;
import info.magnolia.cms.module.ModuleConfig;
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.jar.JarFile;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.log4j.Logger;


/**
 * Module "templating" main class.
 * 
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine implements Module {

	/**
	 * Logger.
	 */
	private static Logger log = Logger.getLogger(Engine.class);

	/**
	 * base path jcr property.
	 */
	private static final String ATTRIBUTE_BASE_PATH = "basePath"; //$NON-NLS-1$

	/**
	 * Module name.
	 */
	protected String moduleName;

	/**
	 * Base path in configuration.
	 */
	protected String basePath;

	/*
	 * (non-Javadoc)
	 * 
	 * @see info.magnolia.cms.module.Module#register(java.lang.String,
	 *      java.lang.String, info.magnolia.cms.core.Content,
	 *      java.util.jar.JarFile, int)
	 */
	public void register(String moduleName, String version, Content moduleNode,
			JarFile jar, int registerState) throws RegisterException {
		// nothing to do
		/*
		 * try {
		 * 
		 * if (true || registerState == Module.REGISTER_STATE_INSTALLATION) {
		 * HierarchyManager hm =
		 * ContentRepository.getHierarchyManager(ContentRepository.CONFIG);
		 * HierarchyManager hmRoles =
		 * ContentRepository.getHierarchyManager(ContentRepository.USER_ROLES);
		 * HierarchyManager hmUsers =
		 * ContentRepository.getHierarchyManager(ContentRepository.USERS);
		 * 
		 * ModuleUtil.registerProperties(hm,
		 * "com.obinary.magnolia.module.dms.config");
		 * ModuleUtil.createPath(hmRoles, "dms", ItemType.CONTENT);
		 * ModuleUtil.registerProperties(hmRoles,
		 * "com.obinary.magnolia.module.dms.roles");
		 * ModuleUtil.createPath(hmUsers, "dms", ItemType.CONTENT);
		 * ModuleUtil.registerProperties(hmUsers,
		 * "com.obinary.magnolia.module.dms.users"); // moveMenuPoint(hm);
		 * 
		 * hm.save(); hmUsers.save(); hmRoles.save(); // install the files
		 * ModuleUtil.installFiles(jar, "dms"); } } catch (Exception e) {
		 * log.error("can' register dms module", e); }
		 */
	}

	/**
	 * @see info.magnolia.cms.module.Module#init(info.magnolia.cms.module.ModuleConfig)
	 */
	public void init(ModuleConfig config) {
		this.moduleName = config.getModuleName();
		this.basePath = (String) config.getInitParameters().get(
				ATTRIBUTE_BASE_PATH);

		// set local store to be accessed via admin interface classes or JSP
		Store.getInstance().setStore(config.getLocalStore());
		
		// register servlet
		
		try
		{
			String servletClassName = FlowDefServlet.class.getName();
			ModuleUtil.registerServlet("FlowDef", servletClassName, new String[]{"/FlowDef"}, "registered by Jackie");
			servletClassName = FlowDefUpload.class.getName();
			ModuleUtil.registerServlet("FlowDefUpload", servletClassName, new String[]{"/FlowDefUpload"}, "registered by Jackie");
		}
		catch(Exception e)
		{
			log.error("Error while loading the xml rpc module",e);
		}

		log.info("****************************************");
		log.info("Module: " + this.moduleName); //$NON-NLS-1$
		log.info(this.moduleName + ": starting OpenWFE"); //$NON-NLS-1$
		try {
			log.debug( "create owfe engine ...");
			new OWFEEngine();// .run();
			log.debug( "create owfe engine ok.");
		

		} catch (Exception e) {
			log.error("An exception arised when creating the engine",e);
		}

		log.info(this.moduleName + ": start OpenWFE OK."); //$NON-NLS-1$      
		log.info("****************************************");

		registerEventListeners();
	}

	String printHMNode(Content node, boolean child) throws Exception {
		String ret = "";
		ret += "node name: " + node.getName() + "\r\n";
		ret += "title: " + node.getTitle() + "\r\n";
		ret += "JCRNode: " + node.getJCRNode().getName() + "("
				+ node.getJCRNode().getPath() + ")" + "\r\n";
		ret += "type: " + node.getNodeType().getName() + "\r\n";
		ret += "Propertie: \r\n";
		Iterator r = node.getNodeDataCollection().iterator();
		while (r.hasNext()) {
			NodeData p = (NodeData) r.next();
			ret += (p.getName() + "=" + p.getString() + "\r\n");
		}
		ret += "\r\n";
		//		
		// List list = node.collectAllChildren();
		// for (int i = 0; i < list.size(); i++){
		// Content n = (Content) list.get(i);
		// ret += "node name: " + n.getName() + "\r\n";
		// ret += "title: " + n.getTitle() + "\r\n";
		// ret += "JCRNode: " + n.getJCRNode().getName() + "(" +
		// n.getJCRNode().getPath() + ")" + "\r\n";
		// ret += "type: " + n.getNodeType().getName() + "\r\n";
		// ret += "Propertie: \r\n";
		// r = n.getNodeDataCollection().iterator();
		// while (r.hasNext()) {
		// NodeData p = (NodeData) r.next();
		// ret += (p.getName() + "=" + p.getString() + "\r\n");
		// }
		// ret += "\r\n";
		// }
		ret += ("\r\n");

		if (child) {
			Iterator it = node.getChildren(ItemType.WORKITEM).iterator();
			while (it.hasNext()) {
				ret += printHMNode((Content) it.next(), true);
			}
		}
		return ret;
	}

	String printJCRNode(Node node, boolean child) throws Exception {

		String ret = "";
		ret += "node name: " + node.getName() + "\r\n";
		ret += "path: " + node.getPath() + "\r\n";
		ret += "Propertie: \r\n";
		// PropertyIterator r = node.getProperties();
		// while (r.hasNext()){
		// Property p = r.nextProperty();
		//			
		// ret += (p.getName() + "=" + p.toString() + "\r\n");
		//			
		// }

		ret += ("defintion: " + node.getDefinition().getName() + "\r\n");
		ret += ("\r\n");

		if (child) {
			NodeIterator it = node.getNodes();
			while (it.hasNext()) {

				ret += printJCRNode(it.nextNode(), true);

			}
		}
		return ret;
	}

	/**
	 * @see info.magnolia.cms.module.Module#destroy()
	 */
	public void destroy() {
		// do nothing
		// @todo remove event listeners?
	}

	/**
	 * Add jcr event listeners for automatic reloading of templates and
	 * paragraphs when content changes.
	 */
	private void registerEventListeners() {

		// automatically reload paragraphs
		registerEventListeners(
				"/" + this.basePath + "/Paragraphs", new EventListener() { //$NON-NLS-1$ //$NON-NLS-2$

					public void onEvent(EventIterator iterator) {
						// reload everything, should we handle single-paragraph
						// reloading?
						// registerParagraphs();
					}
				});

		// automatically reload templates
		registerEventListeners(
				"/" + this.basePath + "/Templates", new EventListener() { //$NON-NLS-1$ //$NON-NLS-2$

					public void onEvent(EventIterator iterator) {
						// reload everything, should we handle single-template
						// reloading?
						Template.reload();
					}
				});
	}

	/**
	 * Register a single event listener, bound to the given path.
	 * 
	 * @param observationPath
	 *            repository path
	 * @param listener
	 *            event listener
	 */
	private void registerEventListeners(String observationPath,
			EventListener listener) {

		log
				.info("Registering event listener for path [" + observationPath + "]"); //$NON-NLS-1$ //$NON-NLS-2$

		try {

			ObservationManager observationManager = ContentRepository
					.getHierarchyManager(ContentRepository.CONFIG)
					.getWorkspace().getObservationManager();

			observationManager.addEventListener(listener, Event.NODE_ADDED
					| Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED,
					observationPath, true, null, null, false);
		} catch (RepositoryException e) {
			log
					.error(
							"Unable to add event listeners for " + observationPath, e); //$NON-NLS-1$
		}

	}

	void testJCREngine() {

		Repository repo = ContentRepository.getRepository("workitems");
		log.info("get repository for workitmes = " + repo);
		HierarchyManager hm = ContentRepository
				.getHierarchyManager("workitems");
		log.info("get HierarchyManager for workitmes = " + hm);
		try {

			// export to file
			File outputFile = new File("d:\\export.xml");
			FileOutputStream out = new FileOutputStream(outputFile);
			hm.getWorkspace().getSession().exportSystemView("/", out, false,
					false);

			Content root = hm.getRoot();
			log.info("root = " + root);
			log.info("--------------HierarchyManager----------:\r\n"
					+ printHMNode(root, true) + "\r\n---------------");

			// create one node
			Content nc = root.createContent("test", ItemType.WORKITEM);
			Node nd1 = root.getJCRNode().addNode("j111crNODE");
			log.info("added node = " + nd1);
			NodeData nd = nc.createNodeData("ID");

			// nd.setValue(new
			// Timestamp(System.currentTimeMillis()).toLocaleString());
			nd.setValue("1");

			hm.save();

			// list content nodes
			Collection items;
			// items = root.getChildren(ItemType.WORKITEM);
			// if (items != null) {
			// log.info("work items number = " + items.size());
			// Iterator it = items.iterator();
			// int i = 0;
			// while (it.hasNext()) {
			// Content tmp = (Content) it.next();
			// log.info("node " + i++);
			// log.info("----------------");
			// log.info("node name = " + tmp.getName());
			// log.info("title = " + tmp.getTitle());
			// log.info("ID= " + tmp.getNodeData("ID").getString());
			// log.info("----------------");
			// }
			//
			// }

			// test query manager
			log.info("------Test query manager--------");
			QueryManager qm = hm.getQueryManager();
			if (qm == null)
				throw new Exception("no query manager for repoistory");
			Query q = qm.createQuery("test", Query.XPATH);
			QueryResult result = q.execute();

			items = result.getContent("workItem");
			log.info("query result = " + items);
			if (items != null) {
				log.info("query result: " + items.size());
				Iterator it = items.iterator();
				while (it.hasNext()) {
					Content n = (Content) it.next();

					log.info(n.getName());
					log.info(n.getNodeData("ID").getString());

				}
			}
			log.info("----------------");
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		/*
		 * // travesal the repository try { Node rn =
		 * hm.getWorkspace().getSession().getRootNode();
		 * 
		 * log.info("--------------repository----------:\r\n" + printJCRNode(rn,
		 * true) + "\r\n---------------"); } catch (Exception e) {
		 * e.printStackTrace(); return; }
		 */
	}
}