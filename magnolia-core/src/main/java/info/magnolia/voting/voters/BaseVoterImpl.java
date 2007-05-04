package info.magnolia.voting.voters;

import info.magnolia.voting.Voter;

/**
 * @author fgiust
 * @version $Revision$ ($Author$)
 */
public abstract class BaseVoterImpl implements Voter {

    /**
     * Enable/disable this cache voter.
     */
    private boolean enabled;

    /**
     * Default constructor.
     */
    public BaseVoterImpl() {
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
