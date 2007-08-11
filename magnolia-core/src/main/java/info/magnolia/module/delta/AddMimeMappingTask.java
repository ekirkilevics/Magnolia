package info.magnolia.module.delta;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.ItemType;
import info.magnolia.module.InstallContext;

import javax.jcr.RepositoryException;


/**
 * Task that adds a mime mapping to <code>server/MIMIMapping</code>.
 * @author fgiust
 * @version $Revision: $ ($Author: $)
 */
public class AddMimeMappingTask extends AbstractRepositoryTask {

    /**
     * Extension (without the <code>.</code>)
     */
    private String extension;

    /**
     * mime type.
     */
    private String mime;

    /**
     * Icon path.
     */
    private String icon;

    /**
     * @param extension Extension (without the <code>.</code>)
     * @param mime mime type.
     * @param icon Icon path.
     */
    public AddMimeMappingTask(String extension, String mime, String icon) {
        super("Add mime mapping task", "Adds a MIME mapping for the " + extension + " extension");
        this.extension = extension;
        this.mime = mime;
        this.icon = icon;
    }

    /**
     * {@inheritDoc}
     */
    protected void doExecute(InstallContext ctx) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = ctx.getHierarchyManager(ContentRepository.CONFIG);
        Content mimeNode = hm.getContent("/server/MIMEMapping");

        if (!mimeNode.hasContent(extension)) {
            Content m = mimeNode.createContent(extension, ItemType.CONTENTNODE);
            m.createNodeData("mime-type").setValue(mime);
            m.createNodeData("icon").setValue(icon);
        }
    }

}
