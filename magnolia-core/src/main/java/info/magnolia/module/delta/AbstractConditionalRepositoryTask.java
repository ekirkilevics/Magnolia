package info.magnolia.module.delta;

import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import org.apache.commons.lang.StringUtils;


/**
 * An abstract implementation of a RepositoyTask that only needs to be executed when a specific node is not found in the
 * repository. Can be used to easily create self-check tasks for mandatory configuration.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public abstract class AbstractConditionalRepositoryTask extends AbstractRepositoryTask {

    /**
     * @param name task name
     * @param description task description
     */
    public AbstractConditionalRepositoryTask(String name, String description) {
        super(name, description);
    }

    /**
     * Returns a path in a repository in the form <code>repository:path</code> (e.g.
     * <code>config:server/activation</code>) that will checked. Only if such path doesn't exist, doExecute() will be
     * called.
     * @return repository:path string
     */
    public abstract String getCheckedPath();

    /**
     * {@inheritDoc}
     */
    public void execute(InstallContext ctx) throws TaskExecutionException {

        boolean executeTask = false;

        String[] tokens = StringUtils.split(getCheckedPath(), ":");
        if (tokens.length != 2) {
            log.error("Invalid checked path " + getCheckedPath() + " in " + this + ". Task will not be performed");
        }
        HierarchyManager hm = ctx.getHierarchyManager(tokens[0]);

        if (hm == null) {
            log.error("Repository "
                + tokens[0]
                + " requested in "
                + this
                + " not available. Task will not be performed");
        }
        try {
            hm.getContent(tokens[1]);
        }
        catch (PathNotFoundException e) {
            // ok, this is expected
            executeTask = true;
        }
        catch (RepositoryException e) {
            throw new TaskExecutionException("Could not execute task: " + e.getMessage(), e);
        }

        if (executeTask) {
            try {
                doExecute(ctx);
            }
            catch (RepositoryException re) {
                throw new TaskExecutionException("Could not execute task: " + re.getMessage(), re);
            }
        }
    }

}
