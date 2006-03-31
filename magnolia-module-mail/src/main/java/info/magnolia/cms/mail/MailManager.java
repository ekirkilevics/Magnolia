package info.magnolia.cms.mail;

import info.magnolia.cms.beans.config.ObservedManager;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.FactoryUtil;

/**
 * Date: Mar 31, 2006
 * Time: 4:10:31 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class MailManager extends ObservedManager {

    private static MailManager instance = (MailManager) FactoryUtil.getSingleton(MailManager.class);

    protected void onRegister(Content content) {

    }

    protected void onClear() {

    }
}
