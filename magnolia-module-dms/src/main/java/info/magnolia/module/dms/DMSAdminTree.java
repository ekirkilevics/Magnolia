/**
 *
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 *
 */

package info.magnolia.module.dms;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.Path;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.AdminTreeMVCHandler;
import info.magnolia.module.dms.beans.Document;
import info.magnolia.module.dms.gui.DMSTreeControl;

import java.util.Collection;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;


/**
 * DMS tree uses type contentNode as document. Type content is used as folder. The mime-type is used to render the documents icon.
 * @author Philipp Bracher
 * @version $Revision$ ($Author$)
 *
 */
public class DMSAdminTree extends AdminTreeMVCHandler {

    private static Logger log = Logger.getLogger(DMSAdminTree.class);

    /**
     * @param name
     * @param request
     * @param response
     */
    public DMSAdminTree(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        setTree(new DMSTreeControl(getRepository(), request, response));
        setConfiguration(new DMSAdminTreeConfig());
    }

    /**
     * Title is related to the Node name
     */
    public String saveValue() {
        String view = VIEW_VALUE;
        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
            // get the node bover it get's renamed
            Content node = hm.getContent(this.getPath());

            view = super.saveValue();
            String saveName = request.getParameter("saveName"); //$NON-NLS-1$
            
            if (saveName.equals("title")) {
                String title = StringUtils.defaultString(request.getParameter("saveValue")); //$NON-NLS-1$
                // set filename if this is a document
                try {
                    if (node.isNodeType(ItemType.CONTENTNODE.getSystemName())) {
                        Document doc = new Document(node);
                        doc.setFileName(title);
                        doc.save();
                    }
                }
                catch (Exception e) {
                    log.error("can't set filename", e);
                }
            }
            
            // add a new version if this is a document
            if (node.isNodeType(ItemType.CONTENTNODE.getSystemName())) {
                Document doc = new Document(node);
                doc.addVersion();
            }
        }
        catch (Exception e) {
            log.error("can't save value", e);
        }
        return view;
    }

    /**
     * set the new filename
     */
    protected String rename(String value) {
        try {
            HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
            Content node = hm.getContent(this.getPath());

            if (Path.getValidatedLabel(value).matches("^-*$")) {
                if (node.getNodeType().getName().equals(ItemType.CONTENT.getSystemName())) {
                    value = "folder";
                }
                else {
                    value = "file";
                }
            }

            String name = super.rename(value);

            if (node.getNodeType().getName().equals(ItemType.CONTENTNODE.getSystemName())) {
                NodeDataUtil.getOrCreate(node,"name").setValue(name);
                node.save();
            }

            return name;
        }
        catch (Exception e) {
            log.error("can't rename node", e);
            return value;
        }
    }

    public String createNode() {
        String view = super.createNode();

        // get all childs
        String createItemType = request.getParameter("createItemType");
        HierarchyManager hm = MgnlContext.getHierarchyManager(this.getRepository());
        Content parentNode;
        try {
            parentNode = hm.getContent(this.getPath());
            Collection childs = parentNode.getChildren(createItemType);

            // set title where not present
            for (Iterator iter = childs.iterator(); iter.hasNext();) {
                Content child = (Content) iter.next();
                if (!child.hasNodeData("title") || StringUtils.isEmpty(child.getNodeData("title").getString())) {
                    NodeDataUtil.getOrCreate(child, "title").setValue(child.getName());
                    NodeDataUtil.getOrCreate(child, "type").setValue("folder");
                    child.save();
                }
            }
        }
        catch (Exception e) {
            log.error("can't set title of the node", e);
        }

        return view;
    }

}