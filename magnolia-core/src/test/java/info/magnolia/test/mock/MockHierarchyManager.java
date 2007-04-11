/**
 * Magnolia and its source-code is licensed under the LGPL.
 * You may copy, adapt, and redistribute this file for commercial or non-commercial use.
 * When copying, adapting, or redistributing this document in keeping with the guidelines above,
 * you are required to provide proper attribution to obinary.
 * If you reproduce or distribute the document without making any substantive modifications to its content,
 * please use the following attribution line:
 *
 * Copyright 1993-2006 obinary Ltd. (http://www.obinary.com) All rights reserved.
 */
package info.magnolia.test.mock;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.core.DefaultHierarchyManager;
import info.magnolia.cms.security.AccessDeniedException;
import info.magnolia.cms.util.ContentUtil;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author philipp
 * @version $Id$
 *
 */
public class MockHierarchyManager extends DefaultHierarchyManager {

    private Map nodes = new HashMap();

    private MockContent root ;

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MockHierarchyManager.class);


    public MockHierarchyManager() {
       root = new MockContent("jcr:root");
       root.setUUID("jcr:root");
       root.setHierarchyManager(this);
    }

    public Content getContent(String path) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content c = (Content) nodes.get(path);
        if( c == null){
            c = root.getContent(StringUtils.removeStart(path, "/"));
            addContent(c);
        }
        return c;
    }

    public void addContent(Content node){
        nodes.put(node.getHandle(), node);
        ((MockContent)node).setHierarchyManager(this);
    }

    public Content createContent(String path, String label, String contentType) throws PathNotFoundException, RepositoryException, AccessDeniedException {
        Content parent = ContentUtil.createPath(getRoot(), StringUtils.removeStart(path, "/"), ItemType.CONTENTNODE);
        return parent.createContent(label, contentType);
    }

    public Content getContentByUUID(String uuid) throws ItemNotFoundException, RepositoryException, AccessDeniedException {
       return getContentByUUID(getRoot(), uuid);
    }

    protected Content getContentByUUID(Content node, final String uuid) {
        if(uuid.equals(node.getUUID())){
            return node;
        }

        for (Iterator iter = ContentUtil.getAllChildren(node).iterator(); iter.hasNext();) {
            Content child = (Content) iter.next();
            Content found = getContentByUUID(child, uuid);
            if(found != null){
                return found;
            }
        }
        return null;
    }

    public MockContent getRoot() {
        return this.root;
    }


    public void setRoot(MockContent root) {
        this.root = root;
    }

    public String toString() {
        final StringBuffer str = new StringBuffer();
        try {
            ContentUtil.visit(getRoot(), new ContentUtil.Visitor(){
                public void visit(Content node) throws Exception {
                    String prefix = "";
                    for(int i =1 ; i <= node.getLevel(); i++){
                        prefix += "  ";
                    }
                    str.append(prefix).append(node.getName()).append("\n");
                    prefix += "  ";

                    for (Iterator iter = node.getNodeDataCollection().iterator(); iter.hasNext();) {
                        NodeData nd = (NodeData) iter.next();
                        str.append(prefix).append(nd.getName()).append(" = ").append(nd.getString()).append("\n");
                    }
                }
            });
        }
        catch (Exception e) {
            log.error("can't print content", e);
        }

        return str.toString();
    }

}
