package info.magnolia.cms.cache.voters;

import info.magnolia.cms.cache.CacheVoter;


/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class BaseCacheVoterImpl implements CacheVoter {

    /**
     * Enable/disable this cache voter.
     */
    private boolean enabled;

    /**
     * Default constructor.
     */
    public BaseCacheVoterImpl() {
        enabled = true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
