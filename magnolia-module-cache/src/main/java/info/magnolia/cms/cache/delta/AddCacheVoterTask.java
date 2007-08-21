package info.magnolia.cms.cache.delta;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;

import java.util.Iterator;
import java.util.Map;

import javax.jcr.RepositoryException;


/**
 * Task that adds a cache voter to <code>/modules/cache/config/voters</code>.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class AddCacheVoterTask extends AbstractRepositoryTask {

    private String name;

    private Class voterClass;

    private Map properties;

    public AddCacheVoterTask(String name, Class voterClass, Map properties) {
        super("New cache voter", "Adds the " + name + " cache voter");
        this.name = name;
        this.voterClass = voterClass;
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = ctx.getHierarchyManager(ContentRepository.CONFIG);
        Content configNode = hm.getContent("/modules/cache/config");

        Content voters = ContentUtil.getOrCreateContent(configNode, "voters", ItemType.CONTENT);

        if (!voters.hasContent(name)) {
            Content m = voters.createContent(name, ItemType.CONTENTNODE);
            m.createNodeData("class").setValue(voterClass.getName());

            if (properties != null) {
                Iterator it = properties.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry) it.next();
                    m.createNodeData((String) entry.getKey(), entry.getValue());
                }
            }
        }
    }

}
