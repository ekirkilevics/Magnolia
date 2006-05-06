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
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.search.Query;
import info.magnolia.cms.core.search.QueryManager;
import info.magnolia.cms.core.search.QueryResult;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.admininterface.AbstractAdminModule;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;

import openwfe.org.ServiceException;
import openwfe.org.engine.impl.expool.SimpleExpressionPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Module "templating" main class.
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine extends AbstractAdminModule {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(Engine.class);

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onRegister(int)
     */
    protected void onRegister(int registerState) throws RegisterException {
        // nothing todo
    }

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
     */
    protected void onInit() {

        try {

            log.debug("create owfe engine ...");
            new OWFEEngine();// .run();

            log.debug("create owfe engine ok.");

        }
        catch (Exception e) {
            log.error("An exception arised when creating the engine", e);
        }

    }

    String printHMNode(Content node, boolean child) throws Exception {
        String ret = "";
        ret += "node name: " + node.getName() + "\r\n";
        ret += "title: " + node.getTitle() + "\r\n";
        ret += "JCRNode: " + node.getJCRNode().getName() + "(" + node.getJCRNode().getPath() + ")" + "\r\n";
        ret += "type: " + node.getNodeType().getName() + "\r\n";
        ret += "Propertie: \r\n";
        Iterator r = node.getNodeDataCollection().iterator();
        while (r.hasNext()) {
            NodeData p = (NodeData) r.next();
            ret += (p.getName() + "=" + p.getString() + "\r\n");
        }
        ret += "\r\n";

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

    void testJCREngine() {

        Repository repo = ContentRepository.getRepository("workitems");
        log.info("get repository for workitmes = " + repo);
        HierarchyManager hm = ContentRepository.getHierarchyManager("workitems");
        log.info("get HierarchyManager for workitmes = " + hm);
        try {

            // export to file
            File outputFile = new File("d:\\export.xml");
            FileOutputStream out = new FileOutputStream(outputFile);
            hm.getWorkspace().getSession().exportSystemView("/", out, false, false);

            Content root = hm.getRoot();
            log.info("root = " + root);

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

            // test query manager
            QueryManager qm = hm.getQueryManager();
            if (qm == null) {
                throw new Exception("no query manager for repository");
            }
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
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void destroy() {
        if (OWFEEngine.getEngine().isRunning()) {
            log.info("Workflow engine is running, trying to stop...");
            try {
                // before try to stop purge and scheduling tasks
                ((SimpleExpressionPool) OWFEEngine.getEngine().getExpressionPool()).stop();
                OWFEEngine.getEngine().stop();
                log.info("Workflow engine successfully stopped");
            }
            catch (ServiceException se) {
                log.error("Failed to stop Open WFE engine");
                log.error(se.getMessage(), se);
            }
        }
    }

}