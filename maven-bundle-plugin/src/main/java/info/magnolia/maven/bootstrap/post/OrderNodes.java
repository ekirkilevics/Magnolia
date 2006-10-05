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
 */
package info.magnolia.maven.bootstrap.post;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.maven.bootstrap.PostBootstrapper;

import org.apache.commons.lang.exception.NestableException;
import org.apache.maven.plugin.logging.Log;


/**
 * Used to order nodes after bootstrapping
 * @author Philipp Bracher
 * @version $Id$
 */
public class OrderNodes implements PostBootstrapper {

    private String repository;

    private String path = "/";

    private String[] nodes;

    private Log log;

    public void execute(String webappDir) throws Exception {

        HierarchyManager hm = MgnlContext.getSystemContext().getHierarchyManager(repository);

        Content parent;
        try {
            parent = hm.getContent(path);
            ContentUtil.orderNodes(parent, nodes);
        }
        catch (Exception e) {
            throw new NestableException("can't order nodes ", e);
        }
    }

    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public String[] getNodes() {
        return nodes;
    }

    public void setNodes(String[] nodes) {
        this.nodes = nodes;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRepository() {
        return repository;
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

}
