package info.magnolia.cms.mail;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.module.admininterface.AbstractAdminModule;

import javax.jcr.RepositoryException;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class Engine extends AbstractAdminModule {

    private static final String SERVER_MAIL_CONFIG = "smtp";

    /**
     * @see info.magnolia.module.admininterface.AbstractAdminModule#onInit()
     */
    public void onInit() throws InitializationException {
        Content config = getConfigNode();

        try {
            Content smtpNode = config.getContent(SERVER_MAIL_CONFIG);
            MgnlMailFactory.getInstance().initMailParameter(smtpNode);
        }
        catch (RepositoryException e) {
            log
                .error(
                    "Missing configuration for {}{}, module is not properly initialized",
                    config.getHandle(),
                    "/smtp");
        }

    }
}
