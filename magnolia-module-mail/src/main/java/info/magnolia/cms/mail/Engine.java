package info.magnolia.cms.mail;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;

import javax.jcr.RepositoryException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class Engine extends AbstractModule {

    public static final String SERVER_MAIL_CONFIG = "smtp";

    public static final String MAIL_TEMPLATES_PATH = "templates";

    static final Logger log = LoggerFactory.getLogger(Engine.class);


    /**
     * Init cache manager
     */
    public void init(Content configNode) throws InvalidConfigException, InitializationException {
        try {
            Content smtpNode = configNode.getContent(SERVER_MAIL_CONFIG);
            Content templateNode = configNode.getContent(MAIL_TEMPLATES_PATH);
            log.info("Loading mail server settings");
            MgnlMailFactory.getInstance().register(smtpNode);
            log.info("Loading mail templates");
            MgnlMailFactory.getInstance().register(templateNode);
            this.setInitialized(true);
        }
        catch (RepositoryException e) {
            log.error("Missing configuration for mail. Module is not properly initialized");
        }
    }
}
