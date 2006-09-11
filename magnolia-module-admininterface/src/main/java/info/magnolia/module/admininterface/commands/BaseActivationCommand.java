/**
 * 
 */
package info.magnolia.module.admininterface.commands;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.ItemType;
import info.magnolia.cms.exchange.Syndicator;
import info.magnolia.cms.util.FactoryUtil;
import info.magnolia.cms.util.Rule;
import info.magnolia.context.MgnlContext;

import org.apache.commons.lang.StringUtils;


/**
 * @author Philipp Bracher
 * @version $Id$
 *
 */
public abstract class BaseActivationCommand extends BaseRepositoryCommand {

    /**
     * You can pass a syndicatorRule to the command (optional)
     */
    public static final String ATTRIBUTE_SYNDICATOR_RULE = "syndicatorRule";
    /**
     * You can pass a syndicator to the command (optional)
     */
    public static final String ATTRIBUTE_SYNDICATOR = "syndicator";
    /**
     * All subnodes of this types are activated imediately (without using the recursion)
     */
    private String itemTypes = ItemType.CONTENTNODE.getSystemName();
    private Rule syndicatorRule;
    private Syndicator syndicator;

    protected Syndicator getSyndicator() {
        if (syndicator == null) {
            syndicator = (Syndicator) FactoryUtil.getInstance(Syndicator.class);
            syndicator.init(
                MgnlContext.getUser(),
                repository,
                ContentRepository.getDefaultWorkspace(repository),
                getSyndicatorRule());
        }
        return syndicator;
    }

    protected Rule getSyndicatorRule() {
        if (syndicatorRule == null) {
            syndicatorRule = new Rule();
            String[] nodeTypes = StringUtils.split(this.getItemTypes(), " ,");
            for (int i = 0; i < nodeTypes.length; i++) {
                String nodeType = nodeTypes[i];
                syndicatorRule.addAllowType(nodeType);
            }
    
            // always activated
            syndicatorRule.addAllowType(ItemType.NT_METADATA);
            syndicatorRule.addAllowType(ItemType.NT_RESOURCE);
        }
        return syndicatorRule;
    }

    public String getItemTypes() {
        return itemTypes;
    }

    public void setItemTypes(String nodeTypes) {
        this.itemTypes = nodeTypes;
    }

    /**
     * @param syndicator the syndicator to set
     */
    public void setSyndicator(Syndicator syndicator) {
        this.syndicator = syndicator;
    }

    /**
     * @param syndicatorRule the syndicatorRule to set
     */
    public void setSyndicatorRule(Rule syndicatorRule) {
        this.syndicatorRule = syndicatorRule;
    }

}
