package info.magnolia.cms.mail;

import info.magnolia.cms.core.Content;
import info.magnolia.module.ModuleLifecycle;
import info.magnolia.module.ModuleLifecycleContext;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailModule implements ModuleLifecycle {
    private static final Logger log = LoggerFactory.getLogger(MailModule.class);

    public static final String SERVER_MAIL_CONFIG = "smtp";

    public static final String MAIL_TEMPLATES_PATH = "templates";

    private Content configNode;

    public Content getConfigNode() {
        return configNode;
    }

    public void setConfigNode(Content configNode) {
        this.configNode = configNode;
    }

    public void start(ModuleLifecycleContext moduleLifecycleContext) {
        try {
            Content smtpNode = configNode.getContent(SERVER_MAIL_CONFIG);
            Content templateNode = configNode.getContent(MAIL_TEMPLATES_PATH);
            log.info("Loading mail server settings");
            MgnlMailFactory.getInstance().register(smtpNode);
            log.info("Loading mail templates");
            MgnlMailFactory.getInstance().register(templateNode);
        } catch (RepositoryException e) {
            log.error("Missing configuration for mail. Module is not properly initialized");
        }
    }

    public void stop(ModuleLifecycleContext moduleLifecycleContext) {
    }
}
