package info.magnolia.cms.mail;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.module.AbstractModule;
import info.magnolia.cms.module.InitializationException;
import info.magnolia.cms.module.InvalidConfigException;
import info.magnolia.cms.module.RegisterException;
import info.magnolia.cms.util.ContentUtil;

/**
 * Date: Mar 30, 2006
 * Time: 5:37:54 PM
 *
 * @author <a href="mailto:niko@macnica.com">Nicolas Modrzyk</a>
 */
public class Engine extends AbstractModule {

    protected void onRegister(int i) throws RegisterException {

    }

    public void init(Content content) throws InvalidConfigException, InitializationException {
        Content node;
        // register uri mappings
        node = ContentUtil.getCaseInsensitive(moduleNode, "config");
        if (node != null) {
            //MailConfigurationManager.getInstance().register(node);
        }
    }
}
