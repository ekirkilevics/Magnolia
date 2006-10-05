package info.magnolia.maven.bootstrap.post;

import javax.jcr.RepositoryException;

import org.apache.commons.lang.exception.NestableException;
import org.apache.maven.plugin.logging.Log;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.maven.bootstrap.PostBootstrapper;


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
