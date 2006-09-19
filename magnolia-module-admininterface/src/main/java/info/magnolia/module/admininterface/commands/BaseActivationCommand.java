/**
 * 
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.context.MgnlContext;


/**
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public abstract class BaseActivationCommand extends RuleBasedCommand {

    /**
     * You can pass a syndicator to the command (optional)
     */
    public static final String ATTRIBUTE_SYNDICATOR = "syndicator";

    private Syndicator syndicator;

    protected Syndicator getSyndicator() {
        if (syndicator == null) {
            syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
            syndicator.init(
                MgnlContext.getUser(),
                this.getRepository(),
                ContentRepository.getDefaultWorkspace(this.getRepository()),
                getRule());
        }
        return syndicator;
    }

    /**
     * @param syndicator the syndicator to set
     */
    public void setSyndicator(Syndicator syndicator) {
        this.syndicator = syndicator;
    }

}
