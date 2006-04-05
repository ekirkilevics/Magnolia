package info.magnolia.cms.mail;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: Apr 4, 2006
 * Time: 8:32:42 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailConfigurationManager extends ObservedManager {

    /**
     * Logger.
     */
    private static Logger log = LoggerFactory.getLogger(MailConfigurationManager.class);

    /**
     * The current implementation of the ParagraphManager. Defeined in magnolia.properties.
     */
    private static MailConfigurationManager instance;//= (MailConfigurationManager) FactoryUtil.getSingleton(MailConfigurationManager.class);

    /**
     * Instantiated by the system.
     */
    public MailConfigurationManager() {
    }

    protected void onRegister(Content node) {
        log.info("Config : Loading email configuration - " + node.getHandle()); //$NON-NLS-1$
    }

    protected void onClear() {
        log.info("Config : Clearing email configuration"); //$NON-NLS-1$
    }

    /**
     * @return Returns the instance.
     */
    public static MailConfigurationManager getInstance() {
        return instance;
    }
}
