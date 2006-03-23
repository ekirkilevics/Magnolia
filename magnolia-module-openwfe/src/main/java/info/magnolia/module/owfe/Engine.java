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
import info.magnolia.cms.module.ModuleUtil;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.module.admininterface.AbstractAdminModule;
import info.magnolia.module.owfe.servlets.FlowDefServlet;
import org.apache.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Repository;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;


/**
 * Module "templating" main class.
 *
 * @author Sameer Charles
 * @author Fabrizio Giustina
 * @version 2.0
 */
public class Engine extends AbstractAdminModule {

    /**
     * Logger.
     */
    private static Logger log = Logger.getLogger(Engine.class);

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

        // register servlet
        try {
            String servletClassName = FlowDefServlet.class.getName();
            ModuleUtil.registerServlet("FlowDef", servletClassName, new String[]{"/FlowDef"}, "registered by Jackie");
        }
        catch (Exception e) {
            log.error("Error while loading the xml rpc module", e);
        }

        log.info("****************************************");
        log.info("Module: " + this.getName()); //$NON-NLS-1$
        log.info(this.getName() + ": starting OpenWFE"); //$NON-NLS-1$
        try {
            log.debug("create owfe engine ...");
            new OWFEEngine();// .run();
            log.debug("create owfe engine ok.");

        }
        catch (Exception e) {
            log.error("An exception arised when creating the engine", e);
        }

        log.info(this.getName() + ": start OpenWFE OK."); //$NON-NLS-1$      
        log.info("****************************************");

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
            log.info("--------------HierarchyManager----------:\r\n" + printHMNode(root, true) + "\r\n---------------");

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
            if (qm == null) {
                throw new Exception("no query manager for repoistory");
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
            log.info("----------------");
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        /*
         * // travesal the repository try { Node rn = hm.getWorkspace().getSession().getRootNode();
         * log.info("--------------repository----------:\r\n" + printJCRNode(rn, true) + "\r\n---------------"); } catch
         * (Exception e) { e.printStackTrace(); return; }
         */
    }

}